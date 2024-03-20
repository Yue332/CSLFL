# CSLFL
Reduction And Cost-Sensitive Learning for Fault Localization

# Building requirements
Ubuntu 20.04.4 LTS

PyTorch: V1.7.1

Java 1.8

# Step 1 Generate spectrum matrix and initial suspicious value of statement calculated using Ochiai formula.

* com.module.Defects4j is used to compile files and get the test cases. This module mainly uses the commands integrated in the Defects4J dataset.                                                                                                                                                                                                                                                  

* com.module.GetBuggyLine modules is used to get faulty statements.

* com.module.Gzoltar is used to generate the spectra and matrix of program.

* com.module.CalculateValueAndGenCSV is used to get the initial suspicious value of statement.


# Step 2 The reduction of passed test cases and calculation of costs.

* com.finalmodule.cost_slfl.PassTestReduction is used to reduce the passed test cases.

* com.finalmodule.cost_slfl.GenerateChangeMatrixFile is used to update the reduced spectral matrix.

* com.finalmodule.cost_slfl.GenerateChangeMatrixSuspValueCSV is the file to generate the suspicious values of statement after we change the execute results of each test cases.

* com.finalmodule.cost_slfl.CostCompute is used to compute the cost of the test case.


# Step 3 The model setting of our approach.

* run.py is for each buggy version of each project, which is repeatedly executed in runtotal.py.
  
* sum.py merges the results for all the buggy versions of one project.
  
* watch.py prints the results.
  
* Model.py is about the model.
  
* Dataset.py is about the dataset.


# Step 4 The calculation of evaluation indicators.

* com.finalmodule.CalculateTopN, com.finalmodule.CalMFR and com.finalmodule.CalMAR are used to calculate the evaluation indicators.


