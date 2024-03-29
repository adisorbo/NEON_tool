
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
- [Related tools](#Related-tools)
- [References](#References) 


## NEON architecture

<img src="images/Architecture.png"> 

NEON encompasses two independent software components: 
1. the **Patterns Finder** automatically identifies relevant rules for detecting natural language patterns occurring in a set of unstructured texts (*i.e., training set*);
2. the **Tagger** exploits the rules stored in an XML document, to automatically label the relevant information appearing in a different set of natural language documents (*i.e., test set*).

In general, to enable the automated analysis of software informal documents, two main phases are required: 
- the *training phase*, in which a a set of software artifacts of a specific type (*e.g.,* app reviews or issue reports) is inspected to identify rules for capturing recurrent NLP patterns. In particular, this phase encompasses the following steps:
    - The end-user provides a set of software artifacts, (i.e., training documents) (point ① in the above Figure).
    - The **Parser** preprocesses the training documents and generates the semantic graph of each sentence present in these documents (point ② in the above Figure).
    - The **PathsFinder** (i) analyzes the semantic graphs generated in the previous step, (ii) finds all recurrent paths in these graphs, and (iii) outputs the rules (in XML format) able to identify such paths (point ③ in the above Figure).
- The *testing phase*, in which the inferred rules are leveraged to recognize the information of interest in a different corpus of software artifacts. More specifically, this phase encompasses the following steps:
    - The end-user provides a set of software artifacts different from the training documents (point ④ in the above Figure)
    - The **Parser** performs sentence splitting, tokenization, and, for each sentence, it generates the Stanford  Dependencies (SD) representation (point ⑤ in the above Figure).
    - The **Classifier** leverages such a representation and the set of rules (defined in an XML file) to detect the presence of text structures that match one (or more) of the defined rules (point ⑥ in the above Figure).
    - All the recognized sentences (i.e., containing  one  or  more  relevant NL patterns) are highlighted using different colors for different categories (point ⑦ in the above Figure).

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

https://user-images.githubusercontent.com/17591346/126037138-41e32a8f-ce68-4992-bbfd-63ea82fb2890.mp4

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

## Related tools

The following tools all leverage _intention mining_ in specific contexts (i.e., development emails, app reviews). We observe that such tools, though effective, have customization and adaptability limitations. NEON overcomes these limitations by providing high adaptability and offering the opportunity to use a single solution for classifying/extracting useful information from different types of software artifacts.
- [DECA](https://spanichella.github.io/tools.html#deca)
- [ARdoc](https://spanichella.github.io/tools.html#ardoc)


## References

- A. Di Sorbo, S. Panichella, C. A. Visaggio, M. Di Penta, G. Canfora, and H. C. Gall: [Exploiting Natural Language Structures in Software Informal Documentation](https://ieeexplore.ieee.org/document/8769918). In IEEE Transactions on Software Engineering, vol. 47, no. 8, pages 1587-1604, 2021.
- A. Di Sorbo, S. Panichella, C. A. Visaggio, M. Di Penta, G. Canfora, and H. C. Gall: [Development Emails Content Analyzer: Intention Mining in Developer Discussions](https://ieeexplore.ieee.org/abstract/document/7371991). In Proceedings of the 30th IEEE/ACM International Conference on Automated Software Engineering (ASE 2015). Lincoln, Nebraska, pages 12-23, 2015.

**If you use this tool in your research, please cite the following papers:**

```
@INPROCEEDINGS{9609223,
  author={Di Sorbo, Andrea and Visaggio, Corrado A. and Di Penta, Massimiliano and Canfora, Gerardo and Panichella, Sebastiano},
  booktitle={2021 IEEE International Conference on Software Maintenance and Evolution (ICSME)}, 
  title={An NLP-based Tool for Software Artifacts Analysis}, 
  year={2021},
  pages={569-573},
  doi={10.1109/ICSME52107.2021.00058}
}

@ARTICLE{8769918,
  author={Di Sorbo, Andrea and Panichella, Sebastiano and Visaggio, Corrado A. and Di Penta, Massimiliano and Canfora, Gerardo and Gall, Harald C.},
  journal={IEEE Transactions on Software Engineering}, 
  title={Exploiting Natural Language Structures in Software Informal Documentation}, 
  year={2021},
  volume={47},
  number={8},
  pages={1587-1604},
  doi={10.1109/TSE.2019.2930519}
}
```

