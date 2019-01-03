package levywalk;

import levywalk.LevyWalkEntity; // エンティティ
import java.io.*;
import java.util.Random;
import java.lang.reflect.Field;

import org.graphstream.algorithm.generator.RandomEuclideanGenerator;
import org.graphstream.algorithm.randomWalk.*;
import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.algorithm.randomWalk.TabuEntity; // 不要クラス
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class RandomWalkOnGraph{
  final int trial; // 試行回数
  final int nodeNum; // ノード数
  final double threshold; // しきい値
  final boolean researchCoverRatio; // カバー率を調べるかどうか
  //final String entityClass; // null -> (TabuEntity)
  final int stepNum; // ステップ数
  final int entityNum; // エンティティ数
  final int remakeNum; // グラフの作り直し回数
  final File file; // ファイル名

  final String entityClass = LevyWalkEntity.class.getName(); // LevyWalk
  //final String entityClass = null; // RandomWalk (TabuEntity)


  Long seed = Long.valueOf(1531334499515691636L); // ランダムシード

  /* 初期設定 */
  RandomWalkOnGraph(Data d){
    this.trial = d.trial;
    this.nodeNum = d.node;
    this.threshold = d.threshold;
    this.researchCoverRatio = d.researchCoverRatio;
    //this.entityClass = d.entityClass;
    this.stepNum = d.step;
    this.entityNum = d.entity;
    this.remakeNum = d.remake;
    this.file = d.file;

    System.out.println("");
    if(this.researchCoverRatio){
      System.out.println("CoverRatio");
    } else {
      System.out.println("ReachRatio");
    }
    //System.out.println("Seed: " + this.seed);
  }

  public void run (){
    /* 連結グラフを作る */
    Graph graph = this.createGraph();
    if(this.trial < 2) graph.display(false); // グラフを描く(複数回行う場合は描かない)
    /* ランダムウォークをする */
    this.randomWalk(graph);
    //this.printGraphInfo(graph);
    
    return;
  }

  /* ランダムウォークをする */
  void randomWalk(Graph graph){
    //RandomWalk rwalk = new RandomWalk(this.seed);
    RandomWalk rwalk = new RandomWalk();
    Toolkit kit = new Toolkit();

    // ランダムウォークの初期設定
    rwalk.setEntityClass(this.entityClass);
    rwalk.setEntityCount(1);

    // ウォーク
    rwalk.init(graph);
    Node targetNode = kit.randomNode(graph);
    Node firstNode = targetNode; // 適当なノードに設定

    for(Node node: graph.getEachNode()){
      if(node.hasAttribute("start")) firstNode = node;
    }
    while(firstNode.getId() == targetNode.getId())
      targetNode = kit.randomNode(graph);
    targetNode.addAttribute("target", "terget");

    for(Integer count = Integer.valueOf(1); count <= stepNum; count++){
      rwalk.compute(); // 1ステップ進める

      // エンティティの初期位置の取得
      if(count == 1 && this.entityClass == null) { // TabuEntityを用いる場合
        for(Edge anEdge: graph.getEachEdge()){
          if(rwalk.getPasses(anEdge) > 0) { // 通過したエッジを取得（）
            Node aNode = anEdge.getTargetNode();
            Node anotherNode = anEdge.getSourceNode();
            if(rwalk.getPasses(aNode) == 0){              // 一端のノードが通過していなければ
              aNode.addAttribute("start", "start");       // その一端のノードが初期位置
            }else{                                        // 通過していれば
              anotherNode.addAttribute("start", "start"); // 他端のノードが初期位置
            }
          }
        }
      }

      if(!this.researchCoverRatio){ // カバー率を調べる場合 -> false
        if(rwalk.getPasses(targetNode) > 0){ // ターゲットに到達した場合
          WriteToFile(count);
          this.coloring(graph, rwalk);
          rwalk.terminate();
          System.out.println("Entity reached TargetNode.");

          return;
        }
      }

    }

    // ターゲットに到達しなかった場合
    this.WriteToFile("null");
    this.coloring(graph, rwalk);
    this.printConfiguration();
    this.getCoverRatio(graph, rwalk);
    rwalk.terminate();
    System.out.println("Entity didn't reach TargetNode.");

    return;
  }

  // 色付け(初期ノード、目的ノード、通過したノード、通過したエッジ、その他のノード、その他のエッジ)
  public void coloring(Graph graph, RandomWalk rwalk)
  {
    // ノード
    for(Node aNode: graph.getEachNode()){
      if(aNode.hasAttribute("start")) {        // 初期位置
        if(rwalk.getPasses(aNode) > 0){        //なおかつ、通過している
          aNode.addAttribute("ui.style","fill-color: orange; size: 6px;"); // オレンジ
        } else{
          aNode.addAttribute("ui.style","fill-color: yellow; size: 6px;"); // 黄
        }

      } else if(aNode.hasAttribute("target")){ // 目的位置
        aNode.addAttribute("ui.style","fill-color: red; size: 6px;");      // 赤

        // カバー率を調べる場合は、これで属性を上書きする
        if(this.researchCoverRatio) aNode.addAttribute("ui.style","fill-color: black; size: 6px;"); // 黒(カバー率を調べる用)

      } else if(rwalk.getPasses(aNode) > 0){   // 通過している 
        aNode.addAttribute("ui.style","fill-color: cyan; size: 6px;");     // シアン

      } else {                                // その他
        aNode.addAttribute("ui.style","fill-color: black; size: 6px;");    // 黒
      }
    }

    // エッジ
    for(Edge anEdge: graph.getEachEdge()){
      if(rwalk.getPasses(anEdge) > 0){          // 通過している
        anEdge.addAttribute("ui.style","fill-color: blue; size: 2px;"); //青
      }else{                                  // 通過していない
        anEdge.addAttribute("ui.style","fill-color: black; size: 1px;");
      }
    }

    return;
  }

  /* 連結グラフを作る */
  private Graph createGraph(){
    Graph graph = new MultiGraph("random walk");
    RandomEuclideanGenerator gen = new RandomEuclideanGenerator();
    Toolkit kit = new Toolkit();

    gen.setRandomSeed(this.seed); // ランダムシードの設定
    gen.addSink(graph);
    gen.setThreshold(threshold);
    int count = -1;
    do{
      graph.clear();
      count++;

      gen.begin();
      for(int i=1; i<nodeNum; i++){
        gen.nextEvents();
      }
      gen.end();

      if(count > 1000){
        System.out.println("Can't create the graph.");

        return null;
      }
    }while(!kit.isConnected(graph));
    System.out.println("graph reset: " + count);
    this.WriteToFile(count);

    return graph;
  }

  /* ファイルに書き出す */
  public void WriteToFile(String str){
    try{
      FileWriter fw = new FileWriter(file, true);
      fw.write(str + ",");
      fw.close();
    }catch(IOException ex){
      ex.printStackTrace();
    }

    return;
  }
  public void WriteToFile(int value){
    try{
      FileWriter fw = new FileWriter(file, true);
      fw.write(value + ",");
      fw.close();
    }catch(IOException ex){
      ex.printStackTrace();
    }

    return;
  }
  public void WriteToFile(){
    try{
      FileWriter fw = new FileWriter(file, true);
      fw.write("\n");
      fw.close();
    }catch(IOException ex){
      ex.printStackTrace();
    }

    return;
  }

  // 探索したノードとエッジの数とその割合
  private void getCoverRatio(Graph graph, RandomWalk rwalk)
  {
    Double nodeNumber = Double.valueOf(0);
    Double edgeNumber = Double.valueOf(0);
    Double allNodeCount = Double.valueOf(graph.getNodeCount()); 
    Double allEdgeCount = Double.valueOf(graph.getEdgeCount()); 

    System.out.println("");
    for(Node aNode: graph.getEachNode())
      if(rwalk.getPasses(aNode) > 0) nodeNumber++;
    System.out.printf("Node: %5.0f / %5.0f, Ratio: %6.3f\n",nodeNumber, allNodeCount, nodeNumber/allNodeCount*100);

    for(Edge aEdge: graph.getEachEdge())
      if(rwalk.getPasses(aEdge) > 0) edgeNumber++;
    System.out.printf("Edge: %5.0f / %5.0f, Ratio: %6.3f\n",edgeNumber, allEdgeCount, edgeNumber/allEdgeCount*100);
    System.out.println("");

    return;
  }

  // ノード、エッジのID、x座標、y座標、通過の有無を出力
  private void printGraphInfo(Graph graph)
  {
    System.out.println("");
    System.out.println("<Node>");
    for(Node aNode: graph.getEachNode())
      System.out.println("ID: " + aNode.getId() + ",x:  " + aNode.getAttribute("x") + ", y: " + aNode.getAttribute("y"));

    System.out.println("");
    System.out.println("<Edge>");
    for(Edge aEdge: graph.getEachEdge())
      System.out.println("ID: " + aEdge.getId());

    return;
  }

  // グラフの構成要素、ウォークの設定
  private void printConfiguration()
  {
    System.out.println("");
    if(this.entityClass != null){
      System.out.println(this.entityClass);
    }else{
      System.out.println("TabuEntity (RandomWalk)");
    }
    System.out.println("Node: " + nodeNum + ", Threshold: " + threshold);
    System.out.println("Step: " + stepNum + ", Entity: " + entityNum);

    return;
  }

}
