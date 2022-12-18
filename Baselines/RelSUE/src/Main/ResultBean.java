package Main;

import java.util.List;

/**
 * 用于返回实验结果的类，包含topk的results以及各部分的时间
 */

public class ResultBean {
    List<Integer> results;
    List<Integer> results1; // 直接根据signific作为权重得到的结果
    long time4FindingMP; // 找mp的时间， 两种赋权方式一致
    long time4GenCandidates; // 生成候选的时间， 两种赋权方式一致
    long time4Train; // 训练时间， 唯一不一致的地方，但是时间可以忽略不计
    long time4Predict;// 预测结果时间， 两种赋权方式一致

    public ResultBean(List<Integer> results, List<Integer> results1, long time4FindingMP, long time4GenCandidates, long time4Train, long time4Predict){
        this.results = results;
        this.results1 = results1;
        this.time4FindingMP = time4FindingMP;
        this.time4GenCandidates = time4GenCandidates;
        this.time4Train = time4Train;
        this.time4Predict = time4Predict;
    }

    public List<Integer> getResults(){
        return results;
    }

    public List<Integer> getResults1(){
        return results1;
    }

    public long getTime4FindingMP(){
        return  time4FindingMP;
    }

    public long getTime4GenCandidates(){
        return  time4GenCandidates;
    }

    public long getTime4Train() {
        return time4Train;
    }

    public long getTime4Predict(){
        return time4Predict;
    }
}
