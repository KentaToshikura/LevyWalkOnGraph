package levywalk;

import levywalk.LevyWalkEntity; // エンティティ
import java.io.*;
import java.util.Random;
import java.lang.reflect.Field;

import org.graphstream.algorithm.generator.RandomEuclideanGenerator;
import org.graphstream.algorithm.randomWalk.Entity;
import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.algorithm.randomWalk.TabuEntity; // 不要クラス
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class RandomWalkOnGraph extends RandomWalk{
  final Integer trial; // 試行回数
  final Integer nodeNum; // ノード数
  final Double threshold; // しきい値
  //final String entityClass; // null -> (TabuEntity)
  final Integer stepNum; // ステップ数
  final Integer entityNum; // エンティティ数
  final Integer remakeNum; // グラフの作り直し回数
  final File file; // ファイル名
  final Boolean researchCoverRatio; // カバー率を調べるかどうか
  final Double permissibleError;
  final Double lambda;
  final Long randomSeed;
  final Integer interval;
  final Boolean debug;

  final String entityClass = LevyWalkEntity.class.getName(); // LevyWalk
  //final String entityClass = null; // RandomWalk (TabuEntity)



  /* 初期設定 */
  public RandomWalkOnGraph(Data d){
    super(d.randomSeed);
    this.trial = d.trial;
    this.nodeNum = d.node;
    this.threshold = d.threshold;
    //this.entityClass = d.entityClass;
    this.stepNum = d.step;
    this.entityNum = d.entity;
    this.remakeNum = d.remake;
    this.randomSeed = d.randomSeed;
    this.file = d.file;
    this.researchCoverRatio = d.researchCoverRatio;
    this.permissibleError = d.permissibleError;
    this.lambda = d.lambda;
    this.interval = d.interval;
    this.debug = d.debug;

    System.out.println("");
    if(this.researchCoverRatio){
      System.out.println("CoverRatio");
    } else {
      System.out.println("ReachRatio");
    }
  }

  public void run (){
    /* 連結グラフを作る */
    Graph graph = this.createGraph();
    WriteToFile("n/e," + graph.getNodeCount() + "," + graph.getEdgeCount());
    WriteToFile();
    if(this.trial < 2) graph.display(false); // グラフを描く(複数回行う場合は描かない)
    if(this.researchCoverRatio){ // カバー率を調べる場合
      WriteToFile("step,node,edge");
      WriteToFile();
    }
    /* ランダムウォークをする */
    this.randomWalk(graph);
    //this.printGraphInfo(graph);

    return;
  }

  /* ランダムウォークをする */
  void randomWalk(Graph graph){
    Toolkit kit = new Toolkit();

    // ランダムウォークの初期設定
    this.setEntityClass(this.entityClass);
    this.setEntityCount(1);

    // ウォーク
    this.init(graph);
    Node targetNode = kit.randomNode(graph);
    Node firstNode = targetNode; // 適当なノードに設定

    for(Node node: graph.getEachNode()){
      if(node.hasAttribute("start")) firstNode = node;
    }
    while(firstNode.getId() == targetNode.getId())
      targetNode = kit.randomNode(graph);
    targetNode.addAttribute("target", "terget");

    for(Integer count = Integer.valueOf(1); count <= stepNum; count++){
      this.compute(); // 1ステップ進める

      // エンティティの初期位置の取得
      if(count == 1 && this.entityClass == null) { // TabuEntityを用いる場合
        for(Edge anEdge: graph.getEachEdge()){
          if(this.getPasses(anEdge) > 0) { // 通過したエッジを取得（）
            Node aNode = anEdge.getTargetNode();
            Node anotherNode = anEdge.getSourceNode();
            if(this.getPasses(aNode) == 0){              // 一端のノードが通過していなければ
              aNode.addAttribute("start", "start");       // その一端のノードが初期位置
            }else{                                        // 通過していれば
              anotherNode.addAttribute("start", "start"); // 他端のノードが初期位置
            }
          }
        }
      }

      if(this.researchCoverRatio){ // カバー率を調べる場合は行わない
        if(count % this.interval == 0){
          WriteToFile(count);
          WriteToFile(this.getValueOfPassedNode(graph).intValue());
          WriteToFile(this.getValueOfPassedEdge(graph).intValue());
          WriteToFile();
        }
      } else{
        if(this.getPasses(targetNode) > 0){ // ターゲットに到達した場合
          WriteToFile(count);
          this.coloring(graph);
          this.terminate();
          System.out.println("Entity reached TargetNode.");

          return;
        }
      }

    }

    // ターゲットに到達しなかった場合
    if(!this.researchCoverRatio){ // カバー率を調べる場合は行わない
      this.WriteToFile("null");
      System.out.println("Entity didn't reach TargetNode.");
    }
    this.coloring(graph);
    this.printConfiguration();
    this.getCoverRatio(graph);
    this.terminate();

    return;
  }

  @Override
  public void init(Graph graph){
    super.init(graph);

      for(Entity anEntity: entities){
        if(anEntity instanceof LevyWalkEntity){
          LevyWalkEntity lwEntity = (LevyWalkEntity) anEntity; // ダウンキャスト

          lwEntity.researchCoverRatio = this.researchCoverRatio;
          lwEntity.permissibleError = this.permissibleError;
          lwEntity.lambda = this.lambda;
          lwEntity.debug = this.debug;
          lwEntity.setRandomSeed(this.randomSeed); // シードの設定
        }
      }

    return;
  }

  // 色付け(初期ノード、目的ノード、通過したノード、通過したエッジ、その他のノード、その他のエッジ)
  public void coloring(Graph graph)
  {
    // ノード
    for(Node aNode: graph.getEachNode()){
      if(aNode.hasAttribute("start")) {        // 初期位置
        if(this.getPasses(aNode) > 0){        //なおかつ、通過している
          aNode.addAttribute("ui.style","fill-color: orange; size: 6px;"); // オレンジ
        } else{
          aNode.addAttribute("ui.style","fill-color: yellow; size: 6px;"); // 黄
        }

      } else if(aNode.hasAttribute("target")){ // 目的位置
        aNode.addAttribute("ui.style","fill-color: red; size: 6px;");      // 赤

        // カバー率を調べる場合は、これで属性を上書きする
        if(this.researchCoverRatio) aNode.addAttribute("ui.style","fill-color: black; size: 6px;"); // 黒(カバー率を調べる用)

      } else if(this.getPasses(aNode) > 0){   // 通過している
        aNode.addAttribute("ui.style","fill-color: cyan; size: 6px;");     // シアン

      } else {                                // その他
        aNode.addAttribute("ui.style","fill-color: black; size: 6px;");    // 黒
      }
    }

    // エッジ
    for(Edge anEdge: graph.getEachEdge()){
      if(this.getPasses(anEdge) > 0){          // 通過している
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

    gen.setRandomSeed(this.randomSeed); // ランダムシードの設定
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
    //this.WriteToFile(count);

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
  private void getCoverRatio(Graph graph)
  {
    Double nodeNumber = this.getValueOfPassedNode(graph);
    Double edgeNumber = this.getValueOfPassedEdge(graph);
    Double allNodeCount = Double.valueOf(graph.getNodeCount()); 
    Double allEdgeCount = Double.valueOf(graph.getEdgeCount()); 

    System.out.println("");
    System.out.printf("Node: %5.0f / %5.0f, Ratio: %6.3f\n",nodeNumber, allNodeCount, nodeNumber/allNodeCount*100);
    System.out.printf("Edge: %5.0f / %5.0f, Ratio: %6.3f\n",edgeNumber, allEdgeCount, edgeNumber/allEdgeCount*100);
    System.out.println("");

    return;
  }

  // 通過したノード数を取得
  public Double getValueOfPassedNode(Graph graph)
  {
    Double nodeNumber = Double.valueOf(0);
    for(Node aNode: graph.getEachNode())
      if(this.getPasses(aNode) > 0) nodeNumber++;

    return nodeNumber;
  }

  // 通過したエッジ数を取得
  public Double getValueOfPassedEdge(Graph graph)
  {
    Double edgeNumber = Double.valueOf(0);
    for(Edge aEdge: graph.getEachEdge())
      if(this.getPasses(aEdge) > 0) edgeNumber++;

    return edgeNumber;
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
