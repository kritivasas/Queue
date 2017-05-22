import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.impl.DeferredObject;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by variablevasas on 5/21/17.
 */
public class Message {
    String message;
    String id;
    ArrayList<Consumer> toBeConsumedBy;
    private SecureRandom random = new SecureRandom();

    private String nextMessageId() {
        return new BigInteger(130, random).toString(32);
    }
    public Message( String _message, ArrayList<Consumer> _toBeConsumedBy){
        this.message = _message;
        this.id = nextMessageId();
        this.toBeConsumedBy = _toBeConsumedBy;
    }

    public Deferred consumeMessage(){
        final Deferred deferred = new DeferredObject();
        final Message self = this;
        for( Consumer consumer: this.toBeConsumedBy ){

            consumer.consume( this ).promise().then(new DoneCallback() {
                public void onDone(Object o) {
                    boolean allConsumed = true;
                    for( Consumer consumerTemp : self.toBeConsumedBy ){
                        if( consumerTemp.lastMessageConsumedId != self.id ){
                            allConsumed = false;
                            break;
                        }
                    }

                    if( allConsumed ) {
                        deferred.resolve("");
                    }
                }
            });
        }
        return deferred;
    }
}

//    function(){
//        consumedFunc();
//        var allConsumed = true;

//        if( allConsumed ) {
//            deferred.resolve();
//        }
//    });