package levywalk;

import java.io.File;

public class Data{

  // 試行回数
  int trial = 1000;

  // ノード数
  int node = 1000;

  // しきい値
  double threshold = 0.05;

  // カバー率を調べるかどうか
  boolean researchCoverRatio = true;

  //エンティティの種類
  String entityClass = null;

  // 最大ステップ数
  int step = 1000;

  // エンティティ数
  int entity = 1;

  // グラフの再構築回数
  int remake = 1000;

  // 出力ファイル
  File file = new File("result.txt");
}
