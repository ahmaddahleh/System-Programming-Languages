package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T>{
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> id_ToConnectionHandler;

    public ConnectionsImpl(){
        this.id_ToConnectionHandler = new ConcurrentHashMap<>();
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        // Connection already exists
    id_ToConnectionHandler.putIfAbsent(connectionId , handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = id_ToConnectionHandler.get(connectionId);
        // Connection not found
        if (handler == null) {
            return false;
        }
       handler.send(msg);
        return true;
    }

    @Override
    public void disconnect(int connectionId) {
        if(id_ToConnectionHandler.containsKey(connectionId)){
            try {
                id_ToConnectionHandler.get(connectionId).close();
                id_ToConnectionHandler.remove(connectionId);
            }catch (IOException ex){}
        }
    }

    public void sendToAll(T msg){
        for(Integer key : id_ToConnectionHandler.keySet()){
            id_ToConnectionHandler.get(key).send(msg);
        }
    }
    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getId_ToConnectionHandler(){
        return id_ToConnectionHandler;
    }
}
