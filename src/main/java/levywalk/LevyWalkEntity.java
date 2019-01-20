package levywalk;

import java.lang.Math;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Random;
import org.graphstream.algorithm.randomWalk.Entity;
import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.algorithm.randomWalk.TabuEntity;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class LevyWalkEntity extends TabuEntity
{
  final Double decimal = Double.valueOf(0.0000001);
  Double counter = Double.valueOf(0.0); // ナンバリング

  Random random = new Random();
  Boolean researchCoverRatio; 
  Double permissibleError; // 許容誤差
  Double lambda; // スケーリングパラメータ,ラムダ
  Boolean debug = false;

  @Override
    public void init(RandomWalk.Context context, Node start)
    {
      super.init(context, start);
      //System.out.println("RandomSeed: " + this.seed); // シードの確認
      start.addAttribute("start", "start");
      return;
    }

  @Override
    public void step()
    {
      levyWalkStep();
      return;
    }

  protected void levyWalkStep()
  {
    if(debug){
      System.out.println("");
      System.out.println("levyWalkStep");
      System.out.println("currentNode " + this.current.getId());
    }
    Integer hopLength = this.getHopLength();
    Integer aValue = this.counter.intValue();            // 整数部はそのままで
    this.counter = Double.valueOf(aValue.doubleValue()); // 少数部を0にする
    if(debug) System.out.println("hopLength " + hopLength + ", counter " + this.counter);
    this.counter += 1.0;

    Double orientation = this.getOrientation(); // 方向を決定
    if(debug) System.out.println("~~~~~~~~~ set Orientation");

    for(Integer i = 1; i <= hopLength; i++) // hopLength分のLevyWalkを繰り返す
    {
      if(debug){
        System.out.println("~~ I N ~~");
        System.out.println("counter " + this.counter + ", currentNode " + this.current.getId());
      }
      ArrayList<Edge> neighbors = this.getNeighbor(this.current); // 隣接エッジを取得
      ArrayList<Edge> possibleNeighbors = new ArrayList<Edge>(0); // 移動可能エッジ
      if(debug){
        System.out.println("~~~~~~~~~ remaining HopLength " + i + ", orientation " + orientation);
        System.out.println("~~~~~~~~~ neighbors have " + neighbors.size() + " elements");
        System.out.printf("~~~~~~~~~ neighbor");
        for(Edge edge: neighbors) System.out.printf(" " + edge + ",");
        System.out.println();
      }

      // 移動可能なエッジを調べる
      if(debug) System.out.println("~~~~~~~~~ ~~ I N ~~");
      while(possibleNeighbors.size() < 1) // 移動可能なエッジを見つけるまで繰り返す
      {
        for(Edge neighbor: neighbors)
        {
          Double error = this.getError(orientation, neighbor); // 方向と隣接エッジとの誤差を取得
          neighbor.addAttribute("error", error);                               // 誤差をそのエッジの属性として追加
          if(debug) System.out.println("~~~~~~~~~ ~~~~~~~~~ error: " + error + ", permissibleError: " + this.permissibleError);
          if(error < this.permissibleError){
            if(debug) System.out.println("~~~~~~~~~ ~~~~~~~~~ possibleNeighbors.add");
            possibleNeighbors.add(neighbor);    // 許容誤差内であれば、移動可能とする
          }
        }

        if(debug) System.out.println("~~~~~~~~~ ~~~~~~~~~ size of possibleNeighbors -> " + possibleNeighbors.size());
        if(possibleNeighbors.size() < 1) { // 移動可能なエッジが存在しない場合
          Integer aPart = Integer.valueOf(this.counter.intValue()); // 整数部を取得
          Double integerPart = Double.valueOf(aPart);               // Doubleに変換

          if((this.counter - integerPart) <= this.decimal){ // 1回目の移動
            if(debug) System.out.println("~~~~~~~~~ ~~~~~~~~~ nothing PN, and first of hop.");
            // 新たに方向を取得する（そして、また移動可能なエッジを調べる）
            orientation = this.getOrientation();
            if(debug) System.out.println("~~~~~~~~~ ~~again~~ ");
          } else {                  // 2回目以降の移動
            if(debug){
              System.out.println("~~~~~~~~~ ~~~~~~~~~ nothing PN, and more than second of hop.");
              System.out.println("~~ OUT ~~ ~~ OUT ~~");
            }
            return;
          }
        }
      }
        
      if(debug) System.out.println("~~~~~~~~~ ~~ OUT ~~");

      // 移動可能なエッジの中からもっとも誤差が小さいエッジを取得
      Edge neighbor = this.getMinimumError(possibleNeighbors);
      this.counter += this.decimal;
      current.addAttribute("counter", this.counter);
      if(debug) System.out.println("~~~~~~~~~ PN of minimumError " + neighbor.getId() + ", error " + neighbor.getAttribute("error") + ", counter " + counter);
      this.cross(neighbor);
      if(debug) System.out.println("~~~~~~~~~ " + neighbor.getId() + " crossed");
      
      if(!this.researchCoverRatio){ // カバー率を調べる場合 -> false
        if(this.current.hasAttribute("target")){
          if(debug) System.out.println("~~ Reached and OUT~~");
          return;
        }
      }

      System.out.printf("\r%3.0fsteps (lw %3.0f%%)", counter, i.doubleValue()/hopLength.doubleValue()*100.0);
    }
    if(debug) System.out.println("~~ OUT ~~");

    return;
  }

  // シードを設定
  public void setRandomSeed(Long seed)
  {
    this.random.setSeed(seed);
    return;
  }

  //隣接エッジを取得
  public ArrayList<Edge> getNeighbor(Node aNode)
  {
    Iterator<? extends Edge> anEdge = aNode.getLeavingEdgeIterator();
    ArrayList<Edge> edges = new ArrayList<Edge>(0);
    while (anEdge.hasNext()) edges.add(anEdge.next()); // 隣接ノードを追加

    return edges;
  }

  // 移動方向の取得
  public Double getOrientation()
  {
    Integer valueInt = Integer.valueOf(this.random.nextInt(360)); // 0<=x<360ので角度(移動方向)を取得
    Double orientation = Double.valueOf(valueInt.doubleValue());  // 取得した角度を少数に変換
    if(debug) System.out.println("getOrientation()|" + " (Integer)orientation " + valueInt + ", (Double)orientation " + orientation);

    return orientation;
  }

  // 角度を取得
  public Double getAngle(Edge anEdge)
  {
    Node aNode = anEdge.getTargetNode(); // 他方のノードを取得
    if(debug) System.out.printf("getAngle() -> curentNode: " + current.getId() + ", neighborNode: " + aNode.getId());
    if(current.getId() == aNode.getId()){ // 現在のノードと取得したノードが同じノードの場合、
      aNode = anEdge.getSourceNode();     // もう一方のノードを取得
    }
    Double x = aNode.getAttribute("x"); // x座標を取得
    Double y = aNode.getAttribute("y"); // y座標を取得
    Double anAngle = Math.toDegrees(Math.atan2(x,y)); // ラジアンではなく、度を取得
    if(debug) System.out.println(", angle " + anAngle + ", x " + x + ", y " + y);
    
    return anAngle;
  }

  // 角度の誤差を取得
  public Double getError(Double anOrientation, Edge anEdge)
  {
    Double anAngle = this.getAngle(anEdge);
    Double anError = anAngle - anOrientation;
    if(debug) System.out.println("getError()|" + " anEdge " + anEdge + ", anAngle - anOrientation = " + anAngle + " - " + anOrientation + ", anError " + anError);
    return Math.abs(anError); // 絶対値で返す
  }

  // 設定したランダムシードより、乱数を生成
  public Double getRandomValue()
  {
    Double aValue = random.nextDouble(); // 乱数を取得
    return aValue; // 0 <= d < 1
  }

  // ステップ長の取得
  public Integer getHopLength()
  {
    Double aHopValue = Math.pow(this.getRandomValue(), (-1)*this.lambda); // x^(-λ) べき関数
    if(aHopValue < 1){
      System.err.println("aHopValue < 1, " + aHopValue);
      System.exit(1);
    } else if(aHopValue > 10000.0){
      aHopValue = 10000.0;
    }
    Integer aHopLength = aHopValue.intValue();
    return aHopLength;
  }

  // 最小のエラーを持つエッジを取得
  public Edge getMinimumError(ArrayList<Edge> anArray)
  {
    Double minimumError = this.permissibleError + Double.valueOf(1.0);
    Edge minimumEdge = anArray.get(0); // 初期化

    for(Edge anEdge: anArray)
    {
      Double error = anEdge.getAttribute("error");
      if(minimumError > error)
      {
        minimumError = error; // 比較演算は使わないほうが良い？？？
        minimumEdge = anEdge;
      }
    }

    return minimumEdge;
  }
}
