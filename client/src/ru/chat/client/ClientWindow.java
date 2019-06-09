package ru.chat.client;

import ru.chat.network.TCPConnection;
import ru.chat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {
    private static final String IP_ADDR = "192.168.0.11" ;
    private static final int PORT = 8189;
    private static final int WIDTH = 400;
    private static final int HEIGTH = 600;

    public static void main(String[] args) {
        //обычно у граф инт есть ограничения по многопоточности
        //у swing можно работать онли из потока етд
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea(); //поле, куда будем писать
    private final JTextField fieldNickname = new JTextField("Marina");//добавить никнейм
    private final JTextField fieldInput = new JTextField();

    private TCPConnection connection;

    private ClientWindow(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);//по центру
        setAlwaysOnTop(true);//не перекрывалось

        log.setEditable(false);//нельзя редактировать
        log.setLineWrap(true);//auto перенос
        add(log, BorderLayout.CENTER);

        fieldInput.addActionListener(this);//чтобы поймать нажатие на клавишу enter
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
        } catch (IOException e) {
            printMsg("TCPConnection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        //проверка на пустую строку
        if(msg.equals("")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText()+": "+msg);
    }

    //можно не синхронизировать. тк одно соединение, нет многопоточности
    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready..");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close..");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("TCPConnection exception: " + e);
    }

    private synchronized void printMsg(String msg){
        //может вызываться из разных потоков
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());//для работы автоскрола
                //каретка в конце
            }
        });
    }
}
