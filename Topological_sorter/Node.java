class Node {
   public int value;
   public Node next;
	
   public Node() {
   	//do nothing
   }
	
   public Node(int value) {
      this.value = value;
   }
	
   public Node(int value, Node next) {
      this.value = value; 
      this.next = next;
   }
	
   public String toString() {
      StringBuilder ret = new StringBuilder();
      Node curr = this;
      while(curr != null) {
         ret.append(curr.value);
         if(curr.next != null) ret.append(" ");
         curr = curr.next;
      }
      return ret.toString();
   }
}