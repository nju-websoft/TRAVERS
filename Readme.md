## Methods & Baselines

- Relevant Search & Entity Set Expansion
  - RelSUE
    - Reference :  Relevance Search over Schema-Rich Knowledge Graphs (WSDM19)
    - Entry : `Baselines/RelSUE/src/InteractiveMain.java`
  - ESER
    - Reference : Entity Set Expansion via Knowledge Graphs (SIGIR17)
    - Codes : `Baselines/ESER+RelSim/ESER/`
  - RelSim
    - Reference : RelSim: Relation Similarity Search in Schema-Rich Heterogeneous Information Networks (SDM16)
    - Codes : `Baselines/ESER+RelSim/RelSim/`
  - TRAVERS
    - Codes : `TRAVERS/TRAVERS/`
- Online Learning To Rank
  - Codes : `Baselines/OLTR/Codes/`
  - PDGD
    - Reference : Differentiable Unbiased Online Learning to Rank(CIKM18)
  - NSGD-DSP
    - Reference : Variance Reduction in Gradient Exploration for Online Learning to Rank (SIGIR19)
  - We combined [PDGD](https://github.com/HarrieO/OnlineLearningToRank) and [NSGD-DSP](https://github.com/sak2km/OnlineLearningToRank) into one framework
  - The requirements are same as the original framework

## Data

- Relevant Search & Entity Set Expansion
  - Data are in `Data/Semantic/`
  - Semantic of each group is in `Data/Semantic/Paths.txt`
  - Experiments data are `dbpedia_*` and `yago_*`
  - HotStart data is in `Data/Semantic/HotStart/` generated with different strategies
  - Parameter validation data are in `Data/Semantic/ParaStudy/` for selecting the values of hyper-parameters
- Online Learning To Rank
  - Datas need to generate by running scripts to change formats
    - `TRAVERS/Factory/FormatToIR.java` (datas, hotstarts)
    - `Baselines/OLTR/DataSets/alpha.py` ($\alpha$-nDCG infos)
  - Put datas into `Baselines/OLTR/DataSets/` 
  - Follow the steps of the original framework