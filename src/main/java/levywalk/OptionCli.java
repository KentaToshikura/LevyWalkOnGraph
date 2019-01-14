package levywalk;

import org.apache.commons.cli.*;
import java.io.File;

public class OptionCli{
  Options options = new Options();
  CommandLine cmdln;

  OptionCli(){
    /* オプションを定義 */
    options.addOption("trial", true, "number of trials");
    options.addOption("node", true, "number of nodes");
    options.addOption("threshold", true, "value of threshold");
    options.addOption("researchCoverRatio", true, "researchCoverRatio");
    options.addOption("entityClass", true, "kind of entity");
    options.addOption("step", true, "number of steps");
    options.addOption("entity", true, "number of entitys");
    options.addOption("remake", true, "upper limit of remake");
    options.addOption("file", true, "file name");
    options.addOption("lambda", true, "value of lambda");
    options.addOption("permissibleError", true, "value of permissibleError");
    options.addOption("randomSeed", true, "value of randomSeed");
    options.addOption("interval", true, "value of interval");
    options.addOption("debug", true, "for debug");
  }

  /* 解析 */
  public void parse(String[] args){
    CommandLineParser line = new DefaultParser();
    try{
      cmdln = line.parse(options, args);
    }catch(ParseException exp){
      System.err.println("ERROR: " + exp.getMessage());
    }
    return;
  }

  /* オプションの値をセットする */
  public void setArguments(Data d){
    String str;
    if((str = cmdln.getOptionValue("trial")) != null)
      d.trial = Integer.parseInt(str);

    if((str = cmdln.getOptionValue("node")) != null)
      d.node = Integer.parseInt(str);

    if((str = cmdln.getOptionValue("threshold")) != null)
      d.threshold = Double.parseDouble(str);

    if((str = cmdln.getOptionValue("researchCoverRatio")) != null)
      d.researchCoverRatio = Boolean.valueOf(str);

    if((str = cmdln.getOptionValue("entityClass")) != null)
      d.entityClass = str;
      //d.entityClass = str.class.getName();

    if((str = cmdln.getOptionValue("step")) != null)
      d.step = Integer.parseInt(str);

    if((str = cmdln.getOptionValue("entity")) != null)
      d.entity = Integer.parseInt(str);

    if((str = cmdln.getOptionValue("remake")) != null)
      d.remake = Integer.parseInt(str);

    if((str = cmdln.getOptionValue("file")) != null)
      d.file = new File((str.endsWith(".csv"))? str : str.concat(".csv"));

    if((str = cmdln.getOptionValue("lambda")) != null)
      d.lambda = Double.parseDouble(str);

    if((str = cmdln.getOptionValue("permissibleError")) != null)
      d.permissibleError = Double.parseDouble(str);

    if((str = cmdln.getOptionValue("randomSeed")) != null)
      d.randomSeed = Long.parseLong(str);

    if((str = cmdln.getOptionValue("interval")) != null)
      d.interval = Integer.parseInt(str);

    if((str = cmdln.getOptionValue("debug")) != null)
      d.debug = Boolean.valueOf(str);

    return;
  }
}
