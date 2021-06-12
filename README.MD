
<img src="images/NEON_animated.gif" width="400"> 

[![License: MIT](https://img.shields.io/badge/license-GPL%20(%3E%3D%203)-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)

A tool for automatically inferring rules aimed at identifying the occurrences of natural language patterns in software informal documentation.
NEON provides a user-friendly GUI. Alternatively, it can also be used programmatically by exploiting its API.  

## Table of Contents

- [NEON architecture](#NEON-architecture)
- [Using Graphic User Interface](#Using-Graphic-User-Interface)
- [Using API](#Using-API)
  - [Training phase](#Training-phase)
  - [Testing phase](#Testing-phase)
- [Notice](#Notice)
- [License](#License)
- [References](#References) 


## NEON architecture

<img src="images/NEON_architecture.png"> 

In general, to enable the automated analysis of software informal documents, two main phases are required: 
- the *training phase*, in which a a set of software artifacts of a specific type (*e.g.,* app reviews or issue reports) is inspected to identify rules for capturing recurrent NLP patterns; and 
- the *testing phase*, in which the inferred rules are leveraged to recognize the information of interest in a different corpus of software artifacts. 

NEON encompasses two independent software components that allow automating both phases: 
1. the **Patterns Finder** automatically identifies relevant rules for detecting natural language patterns occurring in a set of unstructured texts (*i.e., training set*);
2. the **Tagger** exploits the rules stored in an XML document, to automatically label the relevant information appearing in a different set of natural language documents (*i.e., test set*).

An example rule able to recognize a grammatical structure in a generic sentence is reported below: 

```xml
<NLP_heuristic>
    <sentence type="declarative"/>
    <type>aux/neg/dobj/nsubj</type>
    <text>[something] [auxiliary] not open [something].</text>
    <conditions>
        <condition>aux.governor="open"</condition>
        <condition>aux.governor=neg.governor</condition>
        <condition>neg.governor=dobj.governor</condition>
        <condition>dobj.governor=nsubj.governor</condition>
    </conditions>
    <sentence_class>PROBLEM DISCOVERY</sentence_class>
</NLP_heuristic>
```

## Using Graphic User Interface 

Please clone the NEON repository in a local folder of your choice.

The tool uses the following open source Java libraries: 
- version 3.4.1 of the Stanford CoreNLP library 
- version 0.23 of the Efficient Java Matrix Library 
- version 1.0.0 of the java-string-similarity library 
- version 4.4.2 of the simplenlg library 
- version 1.0.1 of the ws4j library 

You can find the needed jar files for running the tool in the ```/lib``` folder of the cloned repository.

Please add these jars and ```NEON.jar``` to the java classpath and run the ```org.neon.main.Main``` class.

Here we provide a running example from command Line:

Running example for Windows systems:
```
javaw -classpath "[MYPATH]/NEON_tool/lib/*;[MYPATH]/NEON_tool/NEON.jar" org.neon.main.Main
```
  
Running example for Unix/Linux/MacOS systems:
```
javaw -classpath "[MYPATH]/NEON_tool/lib/*:[MYPATH]/NEON_tool/NEON.jar" org.neon.main.Main
```

Where ```[MYPATH]``` is the local path in which the repository has been cloned.

A step-by-step example on how to perform the training and testing phases by using the NEON's GUI is reported below.

<img src="images/animated2.gif" width="800">
 
## Using API

Add all the jars contained in the ```/lib``` folder and ```NEON.jar``` to the classpath of the Java project.

### Training phase

A code example using the NEON's API for performing the training phase is reported below: 

```java
import java.io.File;
import java.util.ArrayList;

import org.neon.model.Condition;
import org.neon.model.Heuristic;
import org.neon.pathsFinder.engine.Parser;
import org.neon.pathsFinder.engine.PathsFinder;
import org.neon.pathsFinder.engine.XMLWriter;
import org.neon.pathsFinder.model.GrammaticalPath;
import org.neon.pathsFinder.model.Sentence;


public class Training {
	
	public static void main(String args[]) throws Exception {
		String trainingText = "This app could have a problem on the UI buttons.\n\n"+
								"Another user said that he's having many problems in visualizing png files.";
		
		File outputFile = new File("/heuristics.xml");
		Parser parser = Parser.getInstance();
		ArrayList<Sentence> parsedSentences = parser.parse(trainingText);
		ArrayList<GrammaticalPath> paths = PathsFinder.getInstance().discoverCommonPaths(parsedSentences);
		ArrayList<Heuristic> heuristicsToStore = new ArrayList<Heuristic>();
		
		
		for (GrammaticalPath p: paths) {
			Heuristic heuristic = new Heuristic();
			ArrayList<Condition> conditions = new ArrayList<Condition>();
			for (String cond: p.getConditions()){
				Condition condition = new Condition();
				condition.setConditionString(cond);
				conditions.add(condition);
			}
			heuristic.setConditions(conditions);
			heuristic.setType(p.getDependenciesPath());
			heuristic.setSentence_type(p.identifySentenceType());
			heuristic.setText(p.getTemplateText());
			heuristicsToStore.add(heuristic);
		}
		
		if (!heuristicsToStore.isEmpty()) {
			XMLWriter.addXMLHeuristics(outputFile, heuristicsToStore);
		}
	}
}
```

### Testing phase

A code example using the NEON's API for performing the testing phase is reported below:


```java
import java.io.File;
import java.util.ArrayList;

import org.neon.engine.Parser;
import org.neon.model.Result;


public class Testing {

	public static void main(String args[]) throws Exception {
		String textToClassify = "The system is having several problems.";
		File rulesFile = new File("/heuristics.xml");		
		org.neon.engine.Parser parser = org.neon.engine.Parser.getInstance();
		
		ArrayList<Result> results = parser.extract(textToClassify, rulesFile);
		
		for (Result res: results) {
			System.out.println(res.getSentence()+":"+res.getHeuristic());
		}
		
	}
}
```
 


## Notice

- JDK 13 (or higher) is required to run/use the tool

## License

```
NEON automatically infers rules for identifying natural language 
patterns in software informal documents.
Copyright (C) 2021  Andrea Di Sorbo

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>. 
```

## References

- A. Di Sorbo, S. Panichella, C. A. Visaggio, M. Di Penta, G. Canfora, and H. C. Gall: [Exploiting Natural Language Structures in Software Informal Documentation](https://ieeexplore.ieee.org/document/8769918). IEEE Transactions on Software Engineering (TSE) Journal.
- A. Di Sorbo, S. Panichella, C. A. Visaggio, M. Di Penta, G. Canfora, and H. C. Gall: [Development Emails Content Analyzer: Intention Mining in Developer Discussions](https://ieeexplore.ieee.org/abstract/document/7371991). In Proceedings of the 30th IEEE/ACM International Conference on Automated Software Engineering (ASE 2015). Lincoln, Nebraska, pages 12-23, 2015.

**If you use this tool in your research, please cite the following paper:**

```
@ARTICLE{8769918,
  author={A. {Di Sorbo} and S. {Panichella} and C. A. {Visaggio} and M. {Di Penta} and G. {Canfora} and H. C. {Gall}},
  journal={IEEE Transactions on Software Engineering}, 
  title={Exploiting Natural Language Structures in Software Informal Documentation}, 
  year={2019},
  volume={},
  number={},
  pages={1-1},
  doi={10.1109/TSE.2019.2930519}}
  ```

