<h1>DSV</h1>
is a Java package for <b>Distributional Sense Vectors</b>. The following is a quick documentation of the contained classes.
Arrange for a corpus, create an output folder, and then edit and use experiment.TestExperiment.java for a trial run.

<br><br>

<ul>
<li>AbstractExperiment</li>
	<ul>
	<li>extended by TestExperiment</li>
	</ul>
	
<li>AbstractSetOfIntegers: contains, add, remove, unifyWith, intersectWith</li>
	<ul>
	<li>extended by SetOfIntegers: is represented by a boolean array</li>
	<li>extended by IntegerInterval: lowerBoundary, upperBoundary, contains, overlapsWith, invert, comes in different types, depending on boundaries being limited or unlimited</li>
	</ul>
	
<li>flat text objects</li>
	<ul>
	<li>Word: token, lemma, POS tag, targetElementCount, logTargetElementCount</li>
	<li>Sentence: is represented by a list of Words</li>
	</ul>
	
<li>dependency-based objects</li>
	<ul>
	<li>DepTree: collect nodes in a given neighbourhood above and/or beneath target node</li>
	<li>dependency trees are encoded by DepNode, DepRelation and DepNode objects</li>
	</ul>
	
<li>complex linguistic objects</li>
	<ul>
	<li>ParsedSentence: same sentence represented as flat text and dependency tree</li>
	<li>Document: is represented by a list of ParsedSentences</li>
	</ul>

<li>flat text patterns</li>
	<ul>
	<li>AnyWord: wild card for one Word</li>
	<li>AnyWords: wild card for a sequence of zero or more Words</li>
	<li>TargetWord: marks an element in a sentence or dependency tree as target element</li>
	<li>WordSequenceElement: is extended by Word, AnyWord, AnyWords</li>
	<ul>

<li>LanguagePattern: contextElementCount, logContextElementCount, serves as feature/dimension in the distributional space</li>
	<ul>
	<li>is extended by WordSequence: is a sequence of WordSequenceElements, acts as pattern feature, gets matched against flat sentences to find whether it matches at all or at which element indices it matches, matching with a sentence is somewhat complex since the sequence is a lower-level regular expression</li>
	<li>is extended by DepPattern: is extended DepNode, DepArc,</li>
		<ul>
		<li>is extended by DepTreePattern: is a partial dependency tree, can include target node and wild card nodes, gets matched against dependency trees from data</li>
		</ul>
	</ul>
	
<li>Pipeline: can be executed with run(), contains nodes</li>
	<ul>
	<li>PipelineNode: contains a model object, can propagate signals to previous or next node in pipeline</li>
		<ul>
		<li>a model signals the pipeline node containing it, this pipeline node signals the next pipeline node, that pipeline node signals the model it contains, and so forth</li>
		<li>signalling is always directed by a label object. labels are usually used to address certain model threads.</li>
		<li>a model usually contains threads labelled by distinct label objects. if a model wants to signal one of its threads it adds the signal object to the thread's signal queue. from there the thread can process this signal.</li>
		</ul>
	<li>PipelineSignal</li>
		<ul>
		<li>is implemented by StartSignal: signals a model to start running (a thread)</li>
		<li>is implemented by FinishSignal: signals a model to stop running (a thread)</li>
		<li>is implemented by NotifySignal: notifies (a thread in) a running model</li>
		<li>is also implemented by words, documents, meaning representations, etc.</li>
		</ul>
	</ul>
		
<li>MeaningRepresentation: can compute similarity with MRs of the same type, is collected in BagOfMeaningRepresentations (e.g. one bag per target word)</li>
	<ul>
	<li>is extended by AbstractVector</li>
		<ul>
		<li>is extended by DenseIntegerVector, DenseFloatVector, SparseIntegerVector and SparseFloatVector: use according to needs, e.g. count vectors vs. associationed vectors, e.g. low vs. very high dimensionality</li>
			<ul>
			<li>each is an iterator over VectorEntry objects</li>
			</ul>
		</ul>
	<li>is extended by DistributionOfVectors</li>
	<li>is extended by VectorTree:</li>
	</ul>

<li>AbstractModel: refers to pipeline node containing this model, contains abstract model threads, one signal queue per thread</li>
	<ul>
	<li>is extended by CorpusReader: reads corpus files (raw or g-zipped), one document at a time each containing flat and/or dependency-parsed sentences depending on parameters</li>
		<ul>
		<li>is extended by AgigaCorpusReader: reads corpus files in conll format, one thread per file</li>
		<li>is extended by ConllCorpusReader: reads corpus files in agiga format, one thread per file</li>
		</ul>
	<li>is extended by Counter: gathers feature counts from corpus data for all target words and context windows simultaneously</li>
		<ul>
		<li>is extended by OfflineCounter: first gathers all count vectors, then signals their sum to the pipeline, one thread per corpus file</li>
		<li>is extended by OnlineCounter: each count vector (per context window and target word) is signalled online to the pipeline</li>
		</ul>
	<li>is extended by SequentialClusteringModel: amount of clusters, p-norm, incrementally add count vectors received from OnlineCounter</li>
		<ul>
		<li>is extended by MiniBatchKMeans: implemented after https://www.eecs.tufts.edu/~dsculley/papers/fastkmeans.pdf</li>
		<li>is extended by SequentialAgglomerativeClustering: implemented as a variant of https://papers.nips.cc/paper/5608-incremental-clustering-the-case-for-extra-clusters.pdf</li>
		<li>is extended by SequentialKMeans: also known as online k-means, implented after https://papers.nips.cc/paper/989-convergence-properties-of-the-k-means-algorithms.pdf</li>
		</ul>
	<li>is extended by AssociationFunction: receives a meaning representation, applies itself on the fly to all vectors contained in MR, outputs same type of MR with the associationated vectors, the association function can be LMI, PMI, etc.</li>
	<li>is extended by AbstractApacheModel, AbstractMalletModel and AbstractSSpaceModel depending on which library the model uses: for each target word these models convert the dataset of count vectors into a meaning representation containing sense vectors</li>
		<ul>
		<li>flat models output for each target word a distribution over sense vectors (not necessarily mutually disjoint/orthogonal)</li>
			<ul>
			<li>LdaMallet: latent dirichlet allocation model, amount of topics, burn-in period, amount of iterations</li>
			<li>NmfSSpace: non-negative matrix factorisation, k = number of internal dimensions</li>
			<li>SvdApache: singular value decomposition, k = truncation / number of internal dimensions</li>
			</ul>
		<li>hierarchical models output for each target word a tree of sense vectors (the higher the vector node, the more inclusive/abstract its meaning)</li>
			<ul>
			<li>HacSSpace: hierarchical agglomerative clustering</li>
			<li>HldaMallet: hierarchical latent dirichlet allocation</li>
			</ul>
		</ul>
	<li>is extended by models that don't change the signalled contents, but process them on the fly</li>
		<ul>
		<li>Exporter: writes signalled contents (usually meaning representations) to files, one file per thread</li>
		<li>Printer: prints signalled contents (usually meaning representations) to console</li>
		</ul>
	<li>is extended by Importer: reads meaning representations from files, one file per thread</li>
	<li>is extended by models that use a dataset (and therefore implement ExperimentWithDataset)</li>
		<ul>
		<li>WordSim353: reads the expected word similarities from the dataset (Finkelstein et al., 2002), predicts word similarities using received target word meaning representations, one prediction per context window, evaluates using Spearman correlation</li>
		<li>ML2008: reads the expected phrase similarities from the dataset (Mitchell & Lapata, 2008), predicts phrase similarities using received target word meaning representations, one prediction per context window, evaluates using Spearman correlation</li>
		</ul>
	</ul>
</ul>
