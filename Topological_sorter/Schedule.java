import java.util.Scanner;
import java.io.File;  
import java.io.FileNotFoundException;
import java.util.ArrayList;
   
   
/*
* When given an adjacency matrix that represents a graph,
* prints out nodes in a topological order
* (some items cannot be printed before the ones pointing to them). 
*/
class Schedule {
   public static Node tsortDFS(boolean[][] graph, int startNode) {
   	//error checking on inputs, requires graph, valid starting node, and square matrix
      if(startNode < 0 || startNode > graph.length-1) {
         return null;
      }
   	
      for(int i = 0; i < graph.length; i++) {
         if(graph[i].length != graph.length) {
            return null;
         }
      }
   	
   	//indicators for whether or not a node has been visited
   	//0 = not started, -1 = started not finished, 1 = finished
      int[] visited = new int[graph.length];
   	
   	//the topological ordering
      Node sortedOrder = new Node(); //dummy head
   	
   	//initialize DFS
      boolean dfsDone = false;
   	
      while(!dfsDone) {
      	//visit the start node
         if(!visit(graph, startNode, visited, sortedOrder)) {
         	//graph has no topological sorting
            return null;
         }
      	
      	//make sure all the nodes have been visited
         dfsDone = true;
         int count = 0;
         for(int i = 0; i < graph.length; i++) {
            if(visited[i] == 0){
            //Nested loop, but only triggers if the node was not visited yet
               for(int j=0; j<graph.length; j++) 
               {
                  if(graph[j][i])
                     count++;
               }
               if(count==0){
                  startNode = i;
                  dfsDone = false;
                  break;
               }
               count=0;
            }
         }
      }
   	
   	//return the topological sorting
      return sortedOrder.next; //skip dummy head
   }
	
   private static boolean visit(boolean[][] graph, int currentNode, int[] visited, Node sortedOrder) {
   	//mark current node as started
      visited[currentNode] = -1;
   	
   	//visit each neighbor who hasn't been visited before
      for(int i = 0; i < graph.length; i++) {
      	//if i is a neighbor of the current node...
         if(graph[currentNode][i]) {
         	//check if there's a cycle
            if(visited[i] == -1) {
               return false;
            }
         	
         	//try to visit neighbor if not visited, but stop if neighbor encounters a cycle
            if(visited[i] != 1 && !visit(graph, i, visited, sortedOrder)) {
               return false;
            }
         }
      }
   	
   	//node is finished, mark finished and prepend to topological sorting
      visited[currentNode] = 1;
      sortedOrder.next = new Node(currentNode, sortedOrder.next);
   	
      return true;
   }


   public static void main(String args[])
   {
      try{
         File file = new File(args[0]);
         if (file.length() == 0) 
         { 
            System.out.println("Your file is empty");
            return;
         }
      
         Scanner sc = new Scanner(file);
         
         int nodeCount = sc.nextInt();
         boolean[][] adjMatrix = new boolean[nodeCount][nodeCount];
         ArrayList<String> index = new ArrayList<String>(nodeCount);
         sc.nextLine();
      
         //read in every node into an array that keeps track of which node is at what index
         for(int i=0; i<nodeCount; i++)
         {
            index.add(sc.nextLine());
         }
      
         int edgeCount = sc.nextInt();
         String[] connection = new String[2];
         sc.nextLine();
      
         //split the nodes and connect them by putting them into the adjacency matrix
         for(int i=0; i<edgeCount; i++)
         {
            connection = sc.nextLine().split(",", 2);
            int before = index.indexOf(connection[0]);
            int after = index.indexOf(connection[1]);
            adjMatrix[before][after]=true;
         }
      
         int startNode=0, count=0;
         //first node to have 0 nodes connected to it is the starting node
         for(int i=0; i<nodeCount; i++) 
         {
            for(int j=0; j<nodeCount; j++)
            {
               if(adjMatrix[j][i])
                  count++;
            }
            if(count==0)
            {
               startNode=i;
               break;
            }
            count=0;
         }
      
         Node topSort = tsortDFS(adjMatrix, startNode);
         Node temp = topSort;
      
         //prints the nodes out with ' -> ' in between
         while(temp.next!=null)
         {
            System.out.print(index.get(temp.value)+" -> ");
            temp=temp.next;
         }
         System.out.print(index.get(temp.value));
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
   }
}
