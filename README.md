phyutility
==========
Phyutility (fyoo-til-i-te) is a command line program that performs simple analyses or modifications on both trees and data matrices. Makes use of JADE (PEBLS) and JEBL libraries. Please see the [NEWS] page for info concerning updates, etc.

See [NEWS] and download the new release for amino acid acceptance in the concatenate and cleaning functions (use the -aa argument in -clean and -concat functions).

*Please use this citation when using Phyutility* [http://bioinformatics.oxfordjournals.org/cgi/content/short/24/5/715 Smith, S. A. and Dunn, C. W. (2008) Phyutility: a phyloinformatics tool for trees, alignments, and molecular data. Bioinformatics. 24: 715-716]

Currently it performs the following functions (to suggest another feature, submit an Issue and use the label Type-Enhancement) :

*Trees*

 * rerooting 
 * pruning 
 * type conversion 
 * consensus 
 * leaf stability 
 * lineage movement 
 * tree support 

*Data Matrices*

 * concatenate alignments 
 * genbank parsing 
 * trimming alignments 
 * search NCBI
 * fetch NCBI

*please reroot your trees before performing the leaf stability, lineage movement, or consensus functions -- unrooted functions for these will be incorporated ASAP*

