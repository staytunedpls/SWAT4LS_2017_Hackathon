# Cancer survival prediction:  
Each patient has a survival rate between 0 and 1 (i.e., continuous), which shows how likely they will survive, with 1 being the likeliest. In this part, we will make a prediction of a patient's survival rate. There are three types of genomic data that can be used: DNA methylation data, gene expression data, and miRNA expression data. Participants can use either one of them or combination of them to make the predictive model by training a multimodal deep belief network (MDBN). 
	
  - **SubTypePrediction_DNAMet_ER_with_MLP.scala**: It takes 2 inputs: i) **input_csv_met_type_er.csv** is the data and ii) **label_type_er.csv** is the corresponding label. There are 3 classes to be predicted -i.e. **multinominal classification problem**. 
 - **SubTypePrediction_DNAMet_HER2_with_MLP.scala**: It takes 2 inputs: i) input_csv_met_type_her2.csv is the data and ii) label_type_her2.csv is the corresponding label. There are 4 classes to be predicted -i.e. **multinominal classification problem**. 
- **SubTypePrediction_DNAMet_PGR_with_MLP.scala**: It takes 2 inputs: i) **input_csv_met_type_pgr.csv** is the data and ii) **label_type_pgr.csv** is the corresponding label. There are 4 classes to be predicted -i.e. **multinominal classification problem**.
  
Every Scala script does necessary feature engineering, modelling and simple evaluation. Finally, **accuracy**, **precision**, **recall** and **F1 measure**. It is to be noted, that none of the hyperparametrs are tuned. You can use these simple implementation and tune your training as well as hyperparametrs.  