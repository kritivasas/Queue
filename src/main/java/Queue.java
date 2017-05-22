import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by variablevasas on 5/21/17.
 */
public class Queue {
    ArrayList<Message> queue;
    ArrayList<Consumer> consumers;
    Map<String, Consumer> consumersMap;
    String producer;
    int maxSize = 10;
    boolean isConsuming = false;
    Message currentMessage = null;
    Deferred currentDeferred = null;

    public Queue( int _size ){
        this.queue = new ArrayList<Message>();
        this.consumers = new ArrayList<Consumer>();
        this.consumersMap = new HashMap<String, Consumer>();
        this.producer = null;
        this.maxSize = _size;
        this.isConsuming = false;
        this.currentMessage = null;
        this.currentDeferred = null;
        this.checkForMessages();
    }

    public Consumer registerConsumer( String name, String callbackApi, ArrayList<String> dependencyNames ) throws Exception {
        if( this.consumersMap.containsKey( name ) ) {
            throw new Exception("Already Present");
        }
        boolean allDependenciesMet = true;
        ArrayList<Consumer> dependencyArray = new ArrayList<Consumer>();
        for( String dependencyName : dependencyNames){
            if( this.consumersMap.containsKey( dependencyName ) ){
                dependencyArray.add( this.consumersMap.get( dependencyName ) );
            } else {
                allDependenciesMet = false;
                break;
            }
        }
        Consumer consumer = null;
        if( allDependenciesMet ) {
            consumer = new Consumer( name, callbackApi, dependencyArray );
            this.consumers.add( consumer );
            this.consumersMap.put( name , consumer );
        } else {
            throw new Exception( "Dependency/Dependencies not present" );
        }
        return consumer;
    }

    public boolean isFull(){
        return this.queue.size() >= this.maxSize;
    }

    public Message registerMessage( String _message ) throws Exception {
        if( this.isFull() ) throw new Exception("Queue full");
        Message message = new Message( _message, this.consumers );
        this.queue.add( message );
        this.checkForMessages();
        return message;
    }

    public Deferred checkForMessages(){
        if( this.queue.size() == 0 ) return null;
        if( this.isConsuming ){
            return this.currentDeferred;
        }
        final Queue self = this;
        if( this.queue.size() > 0 ) {
            this.currentMessage = this.queue.remove(0);
            this.isConsuming = true;
            this.currentDeferred = this.currentMessage.consumeMessage();
            Promise promise = this.currentDeferred.promise();
            promise.then(new DoneCallback() {
                public void onDone(Object o) {
                    self.isConsuming = false;
                    self.currentDeferred = null;
                    self.checkForMessages();
                }
            });
            promise.fail(new FailCallback() {
                public void onFail(Object o) {
                    self.queue.add(self.currentMessage);
                    self.isConsuming = false;
                    self.currentDeferred = null;
                    self.checkForMessages();
                }
            });
        }
        return this.currentDeferred;
    }
}
