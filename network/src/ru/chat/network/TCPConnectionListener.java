package ru.chat.network;

public interface TCPConnectionListener {
    //рассмотрим все возможные события
    //запустили соединение и оно уже готово, можем с ним работать
    void onConnectionReady(TCPConnection tcpConnection);
    //соединение приняло строку
    void onReceiveString(TCPConnection tcpConnection, String value);
    //соед оборвалось
    void onDisconnect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection, Exception e);

}
