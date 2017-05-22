# Queue: ( size:int )

**apis:**
  * registerConsumers: call with name:String, callBackUrl:String, DependencyNames:ArrayList<String> <br/>
  * registerMessage: queue a message with message:String <br/>
  * checkForMessages: to run a interval counter which checks for messages on Queue and makes it consume it <br/>
  * isFull: limit of queue has Reached <br/>
