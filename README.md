# LevyWalkOnGraph

#実行方法
java -jar levywalk.jar data.txt

#data.txtの記述例
trial 1
node 1000
threshold 0.05
researchCoverRatio true
entityClass LevyWalk
step 1000
entity 1
remake 1000
file result
permissibleError 20.0
lambda 1.2
interval 1000

#オプション : 説明 : 記述方法
trial : 試行回数 : Integer
node : ノード数 : Integer
threshold : しきい値 : Double
researchCoverRatio : カバー率の調査(true),到達率の調査(false) : Boolean
entityClass : LevyWalk もしくは RandomWalk : String
step : ステップ数 : Integer
entity : エンティティ数 : Integer
remake : グラフの再構成回数の上限 : Integer
file : 出力ファイル名(.csv) : String
permissibleError : 許容誤差 : Double
lambda : パラメータ : Double
interval : カバー率を調査するstep数の間隔 : Integer
