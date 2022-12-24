## Code

- Methods for Relevance Search and Entity Set Expansion
  - TRAVERS
    - Code : `TRAVERS/TRAVERS/`
  - RelSim
    - Reference : RelSim: Relation Similarity Search in Schema-Rich Heterogeneous Information Networks (SDM'16)
    - Code : `Baselines/ESER+RelSim/RelSim/`
  - RelSUE
    - Reference :  Relevance Search over Schema-Rich Knowledge Graphs (WSDM'19)
    - Entry : `Baselines/RelSUE/src/InteractiveMain.java`
  - ESER
    - Reference : Entity Set Expansion via Knowledge Graphs (SIGIR'17)
    - Code : `Baselines/ESER+RelSim/ESER/`
- Methods for Online Learning To Rank
  - Code : `Baselines/OLTR/Codes/`
  - PDGD
    - Reference : Differentiable Unbiased Online Learning to Rank(CIKM'18)
  - NSGD-DSP
    - Reference : Variance Reduction in Gradient Exploration for Online Learning to Rank (SIGIR'19)
  - We combined [PDGD](https://github.com/HarrieO/OnlineLearningToRank) and [NSGD-DSP](https://github.com/sak2km/OnlineLearningToRank) into one framework
  - The requirements are the same as the original framework

## Data

- Relevance Search & Entity Set Expansion
  - Data are in `Data/Semantic/`
  - The semantics of relevance (i.e., meta-paths) of each query group is in `Data/Semantic/infos/Paths.txt`
  - Gold-standard answer entities for each query are in `Data/Semantic`
    - The first line of every two lines contains the ID of a query entity
    - The second line of every two lines contains the IDs of the gold-standard answer entities (the number after the colon can be ignored)
  - The mappings between entity ID and entity URI are available at this [link](https://zenodo.org/record/7480114#.Y6ceqXZBwQ8).
  - To implement a warm start, answer entities presented to the user in the first iteration are in `Data/Semantic/WarmStart/`
    - Directory hierarchy means `"QueryGroup"/"MethPathWeightingMethod"/"QueryLocalID"`
    - In each folder there are two files:
      - "data.txt" contains the ID of the query entity and the IDs of answer entities to be presented
      - "model.txt" can be ignored
    - Please note the following mapping between the names of MethPath Weighting Method in the data and in the paper
      - RI - RelInfo
      - PI - PathInfo
      - SC - Promise
  - The validation set for tuning hyperparameters is in `Data/Semantic/ParaStudy/`
    - The file format is the same as that of the gold standard
- Online Learning To Rank
  - For these methods, data need to be reformed by running the following scripts
    - `TRAVERS/Factory/FormatToIR.java` (datas, warmstarts)
    - `Baselines/OLTR/DataSets/alpha.py` ($\alpha$-nDCG infos)
  - Put data into `Baselines/OLTR/DataSets/` 
  - Follow the steps of the original framework
- Please note the following mapping between the names of query groups in the data and in the paper
  - dbpedia_rs1b - DS1
  - dbpedia_rs2b - DS2
  - dbpedia_rs3b - DS3
  - dbpedia_rs4b - DS4
  - dbpedia_rs5b - DS5
  - dbpedia_11b - DC6
  - dbpedia_21b - DC7
  - dbpedia_21o - DC8
  - dbpedia_22b - DC9
  - yago_rs1b - YS1
  - yago_rs2b - YS2
  - yago_rs3b - YS3
  - yago_rs4b - YS4
  - yago_rs5b - YS5
  - yago_11b - YC6
  - yago_21b - YC7
  - yago_21o - YC8
  - yago_22b - YC9
