package ru.chat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

//пишем универсальный класс и для клиента, и для сервера => use abstraction: интерфейс
//многопоточный =>synchronized, для безопасного обращения из разных потоков
public class TCPConnection {
    //основной класс соединения
    private final Socket socket;
    private final Thread rxThread; //будет слушать входящее сообщение
    //если строка прилетела, то будет сгенерировано событие ->событийная система
    //нужны потоки ввода/вывода, у нас для строк
    private final BufferedReader in;
    private final BufferedWriter out;
    private final TCPConnectionListener eventListener;
//оокет создается внутри
    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException{
        this(eventListener, new Socket(ipAddr, port));//вызываем другой конструктор
    }

    //конструктор для соединения (если снаружи кто-то создал сокет)
    //1-ый создает по сокету соединение
    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        //у сокета нужно получить входящий/исходящий поток, чтобы принмать символы
        //умеет генерировать исключения. в джаве есть обрабатываемые и еобрабатываемые  искл
        //IOException - cheked искл, должны перехватить и обработать или указать в сигнатуре метода,тогда перехват ложиться на того, кто вызывает этот метод
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        //получили простой поток ввода, на его основе создали InputStreamReader и обернули и на его основе создали класс BufferedReader
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        //создаем и запускаем поток, который будет слушать все входящее
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //тут слушаем входящее соединение
                try {
                    eventListener.onConnectionReady(TCPConnection.this);//если просто this, то будет пытаться передать экземпляр анонимного класса
                    //тут же получаем экземпляр обрамляющего класса
                    while (!rxThread.isInterrupted()){//пока не прерван
                        String msg = in.readLine();//читаем строку
                        eventListener.onReceiveString(TCPConnection.this, msg);
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });//должен что-то выполнять =>передаем экземпляр класса с реализацией Runnable, например, анонимный класс
        rxThread.start();
    }

//метод для отправки сообщения
    public synchronized void sendString(String value){
        try {
            //записываем в буфер
            out.write(value + "\r\n");//не добавляется признака конца строки=>add возврат каретки и перевод строки - стандартный символ нью строки
            out.flush();//сбрасывает все буферы и отправляет по сети
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();//тем самым устанавливаем флаг
        }
    }

    //оборвать соединение
    public synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }
//чтобы в логах было видно, кто подключ отключ
    @Override
    public String toString(){
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
