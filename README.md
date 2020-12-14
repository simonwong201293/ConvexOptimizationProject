# ELEC5470/IEDA6100A - Convex Optimization Final Project, Hong Kong University of Science & Technology
## Resource-Aware Scheduling on HeterogeneousServer with Convex Optimization
### Author: Simon Wong, Linus Wong (13/12/2020)

**Abstract**

With growing demands for resource-demanding jobs in different technologies like Deep Learning, Big Data and Internet of Things, job scheduling in the heterogeneous server are rapidly being adopted. To ensure jobs are completed within the given deadline, different servers need to be standby to ensure the jobs which fit the provided environment can be solvedtimely. However, maintaining heterogeneous servers to support a considerable variety of types of jobs is resource exhaustive.Optimizing the resource consumption, significantly reducing the maximum flowtime, and maintaining the systems’ reliabilityis  essential  to  catalyze  a  generic  adoption  of  job  scheduling  in  the  heterogeneous  server  in  more  businesses  and  real-life applications. In this paper, we propose a formula that can be convex optimized to minimize the maximum flowtime ofthe servers with known jobs, thereby reducing the overall resource consumption. Furthermore, our paper investigates theperformance variation when our proposed formula is applied in a single server, multiple servers, and a heterogeneous serverscenario. To prove the proposed formulation’s performance, we have experimented with a computer simulation comparingsome typical benchmarks to demonstrate that we could reduce the maximum flowtime while retaining the reliability.

**Keyword**

Resource-aware, Job Scheduling, Heterogeneous System, Convex Optimization

**Description**

This repository contains:
* The solution generation code (in Matlab)
* The generated solution based on generated job and server sets (By solution generator)
* The experiment simulation code (in Java)
* The generated job and server sets (By simulation program)
* The simulation result (in JSON)

**Contribution**

1. Proposed formulation to minimize the maximum flow time, which is solvable by convex optimization
1. Demonstrated performance enhancement of the proposed formulation through a computer simulation has shown to minimize the maximum flowtime within the specific deadlines

**Overall Numeric Result**

1. 22.9% reductionin a single server maximum job flowtime
1. 58.5% reduction in multiple identical servers maximum job flowtime
1. Competitive algorithm in heterogeneous servers maximum job flowtime with a 17.4% reduction in average job flowtime

**Acknowledgement**

This work was initiated by ELEC5470/IEDA6100A - Convex Optimization at Hong Kong University of Science & Technol-ogy.
