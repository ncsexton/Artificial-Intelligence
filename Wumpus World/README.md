# Wumpus World Simulator

Simulator for the AI Wumpus World game in C++ with Python wrapper.

The Wumpus Simulator is a simple framework for simulating the Wumpus World
described in Russell and Norvig's "Artificial Intelligence: A Modern
Approach".

The simulator uses an agent written in either c++ or python. My agent and its changes are represented in the C Agent files.

## Quick Start

To try out the simulator, install the code on a UNIX system (or a system that
has the 'make' program installed and a C++ compiler). Type 'make' to build the
'wumpsim' executable. Then, type './wumpsim'. You should see a
randomly-generated 4x4 world, information about the game state, and a prompt
for the next action. When the game is over, scoring information is provided.

Simulator Options
-----------------

The wumpus simulator takes a few options, as described below.

`-size <N>` lets you to set the size of the world to NxN (N>1). Default is 4.

`-trials <N>` runs the simulator for N trials, where each trial generates a new
wumpus world. Default is 1.

`-tries <N>` runs each trial of the simulator N times, giving your agent
multiple tries at the same world. Default is 1.

`-seed <N>` lets you set the random number seed to some positive integer so that
the simulator generates the same sequence of worlds each run. By default, the
random number seed is set semi-randomly based on the clock.

`-world <file>` lets you play a specific world as specified in the given file.
The -size option is ignored, and each try and trial uses the same world. The
format of the world file is as follows (all lowercase, must appear in this
order):

```
size N
wumpus N N
gold N N
pit N N
pit N N
...

```
where N is a positive integer. Some error checking is performed. A sample
world file is provided in testworld.txt.

## ACKNOWLEDGEMENTS

Thanks to Dr. Larry Holder for the simulator!  (holder@wsu.edu).

