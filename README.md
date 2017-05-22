# Queue: ( size:int )

apis:
  registerConsumers: call with name:String, callBackUrl:String, DependencyNames:ArrayList<String>
  registerMessage: queue a message with message:String
  checkForMessages: to run a interval counter which checks for messages on Queue and makes it consume it.
  isFull: limit of queue has Reached
