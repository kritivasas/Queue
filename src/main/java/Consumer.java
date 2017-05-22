import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.impl.DeferredObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by variablevasas on 5/21/17.
 */
public class Consumer {
    String name;
    String apiurl; //_callback;
    ArrayList<Consumer> dependencies;// = _dependencies;
    String lastMessageConsumedId;
    Deferred deferred = null;

    boolean isConsuming = false;
    public Consumer( String name, String _callbackApi, ArrayList<Consumer> _dependencies ){
        this.name = name;
        this.apiurl = _callbackApi;
        this.dependencies = _dependencies;
    }

    public Deferred checkDependencyConsumption(final Message message, final int i ){
        if( i >= this.dependencies.size() ) return new DeferredObject().resolve("invalid i");
        final Deferred deferred = new DeferredObject();
        final Consumer self = this;
        if( this.dependencies.get(i).lastMessageConsumedId != message.id ){
            this.dependencies.get(i).consume(message).promise().then(new DoneCallback() {
                public void onDone(Object o) {
                    if( i < ( self.dependencies.size() - 1 )){
                        int nextI = i + 1;
                        self.checkDependencyConsumption(message, nextI);
                    } else {
                        deferred.resolve("done");
                    }
                }
            });
        } else {
            deferred.resolve("done");
        }
        return deferred;
    }

    public Deferred consume(final Message message ){
        if(this.isConsuming && this.deferred != null) return this.deferred;
        else {
            this.isConsuming = false;
            this.deferred = null;
        }

        this.deferred = new DeferredObject();
        final Consumer self = this;
        this.checkDependencyConsumption( message, 0 ).promise().done(
            new DoneCallback() {
                public void onDone(Object o) {
                    try {
                        self.callback();
                        self.lastMessageConsumedId = message.id;
                        self.isConsuming = false;
                        self.deferred.resolve("done");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        );
        return this.deferred;
    }

    public void callback() throws IOException {
        URL url = new URL(this.apiurl);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("POST");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
    }
}
