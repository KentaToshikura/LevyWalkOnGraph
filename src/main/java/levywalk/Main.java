package levywalk;

import java.io.*;
import java.util.Random;

public class Main{
  public static void main(String[] args){
    String[] arguments = fileRead(args[0]); // 入力ファイルを解析
    if(arguments == null) {
      System.out.println("Failed to parse the file.");
      return;
    }

    OptionCli options = new OptionCli(); // オプションを定義
    Data d = new Data();
    options.parse(arguments); // 引数を解析
    options.setArguments(d); // 引数をセット
   
    if(d.graphSeed.equals(0L)){
      if(d.walkSeed.equals(0L)){ // グラフシードと探索シードが設定されていない
        for(Integer i = 0; i < d.trial; i++){
          d.graphSeed = new Random().nextLong();
          d.walkSeed = new Random().nextLong();
          RandomWalkOnGraph rwGraph = new RandomWalkOnGraph(d);
          rwGraph.run();
          rwGraph.WriteToFile(); // 改行を追加
        }
      } else { // 探索シードのみが設定されている
        for(Integer i = 0; i < d.trial; i++){
          d.graphSeed = new Random().nextLong();
          RandomWalkOnGraph rwGraph = new RandomWalkOnGraph(d);
          rwGraph.run();
          rwGraph.WriteToFile(); // 改行を追加
        }
      }
    } else {
      if(d.walkSeed.equals(0L)){ // グラフシードのみが設定されている
        for(Integer i = 0; i < d.trial; i++){
          d.walkSeed = new Random().nextLong();
          RandomWalkOnGraph rwGraph = new RandomWalkOnGraph(d);
          rwGraph.run();
          rwGraph.WriteToFile(); // 改行を追加
        }
      } else { // グラフシードと探索シードが設定されている
        for(Integer i = 0; i < d.trial; i++){
          RandomWalkOnGraph rwGraph = new RandomWalkOnGraph(d);
          rwGraph.run();
          rwGraph.WriteToFile(); // 改行を追加
        }
      }
    }
    System.out.println("\noutput the result to \""+ d.file.getName() +"\"");

    return;
  }

  /* データファイルを解析 */
  private static String[] fileRead(String fileName){
    File file = new File(fileName);
    try{
      BufferedReader br = new BufferedReader(new FileReader(file));
      String str = "";
      String line;
      while((line = br.readLine()) != null)
        str += line + " ";
      br.close();

      String[] strs = str.split(" ");
      for(int i = 0; i < strs.length; i += 2 )
        strs[i] = "-" + strs[i];
      return strs;
    }catch(FileNotFoundException e){
      System.out.println(e);
    }catch(IOException e){
      System.out.println(e);
    }
    return null;
  }
}
