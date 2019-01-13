package levywalk;

import java.io.File;
import java.util.Random;

public class Data{

  // 試行回数
  Integer trial = 1000;

  // ノード数
  Integer node = 1000;

  // しきい値
  Double threshold = 0.05;

  // グラフの再構築回数
  Integer remake = 1000;

  // 出力ファイル
  File file = new File("result.txt");

  // カバー率を調べるかどうか
  Boolean researchCoverRatio = true;

  //エンティティの種類
  String entityClass = null;

  // 最大ステップ数
  Integer step = 1000;

  // エンティティ数
  Integer entity = 1;

  // ラムダ
  Double lambda = 1.2;

  // 許容誤差
  Double permissibleError = 20.0;

  // ランダムシード
  Long randomSeed = new Random().nextLong();

  // 刻み幅
  Integer interval;

  Boolean debug = false;
}
