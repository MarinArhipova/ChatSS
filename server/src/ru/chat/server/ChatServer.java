package ru.chat.server;

import ru.chat.network.TCPConnection;
import ru.chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

//2 roles
public class ChatServer implements TCPConnectionListener {
    public static void main(String[] args) {//psvm
//сервер принимает входящее соединение(сообщение), рассылает другим сообщение
        //сервер сокет умеет слушать,умеет принмать его, создавать объект сокета
        //сокет -  с его помощью соединение можно устанавливать
        new ChatServer();
    }

    //список соединений
    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private ChatServer(){
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(8189)){//умеет слушать порт и принимать входящее соединение
            while(true){
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e){
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(ru.chat.network.TCPConnection tcpConnection) {
        //если соед готово, то оповещаем всех об этом
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: " + tcpConnection);
        //т.к. складываем объект со строкой, то неявно вызывается метод toString, который мы переопределили
    }

    @Override
    public synchronized void onReceiveString(ru.chat.network.TCPConnection tcpConnection, String value) {
        //если приняли строку, то рассылаем ее всем клиентам
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(ru.chat.network.TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(ru.chat.network.TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String value){
        System.out.println(value);
        final int cnt = connections.size();
        //проходим по всему списку соединений и отправляем это сообщение
        for (int i=0; i < cnt; i++) connections.get(i).sendString(value);
    }
}
