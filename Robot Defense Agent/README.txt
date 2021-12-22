Nathan Sexton, Mateo Gannod, Jackson Hidley

Preface: A very peculiar issue occured when working on this program. My desktop computer (Nathan) ran the program
so slowly that any tests I ran on it were incredibly negative. The issue seemed to be that, the movement of the insects 
would grind to a near halt, but the game's clock would continue running normally, so more bugs would spawn before the
first bug even got close to the tower. Interestingly, the game would resume normal speed when all towers were in the 
process of capturing a bug. This was not an issue with my agent, as it also occurred when I ran the provided agent.

Fortunately, when I ran the same code from my laptop, everything ran as it should, so all data collected from trials
that I mention in this readme are from trials conducted on the laptop that ran everything properly.

Thank you!

_______________________________________________________________________________________________________________________________


Part 1
a) Mateo and I figured that the obvious way to have the tower decide to disable the vacuum to save power is for the tower
to check if there are even any bugs within range that it could potentially capture in the first place.
To do this, I created a new method, checkRadiusForBugs, which would check the cells surrounding each tower,
and return a false boolean if there are no bugs within range of the tower. This method is called within the
Agent's step method, so it checks for bugs every time the state is changed. If the state does not change,
then there is no reason for the Agent to continue checking if there are bugs or not, because as soon as a 
bug comes within range/leaves the vacuum range, it will update the state and check for bugs, then deciding
if the power should be shut down.


b) Initially, my idea was to consider powers 2 and 4 in all directions as possible actions, and Mateo's 
idea was to consider all power levels in all directions. Ultimately, after running the game countless times
and getting a better idea of what power levels were even worth considering, we decided to settle on using powers
2, 3, and 4 in all directions as possible choices.


c) I tested the head to head stats of the two agents in three separate trials, with the only current 
modifications to my agent being the changes to controlling the power on or off, and the option to 
choose the second power level as opposed to only the max power. These trials were completed on the 
four towers level. The results were as followed:

Trial One:
	Our Agent: 40 bug captures, 129 escaped bugs
	Base Agent: 57 bug captures, 59 escaped bugs
Trial Two:
	Our Agent: 60 bug captures, 160 escaped bugs
	Base Agent: 41 bug captures, 63 escaped bugs
Trial Three:
	Our Agent: 42 bug captures, 165 escaped bugs
	Base Agent: 19 bug captures, 87 escaped bugs

In evaluating these results, I am assuming that a "good" result would require not only the capturing 
of bugs, but also preventing bugs from reaching the exit at all. Looking at the data from this 
perspective, I will value a captured bug as 1 point, and an escaping bug as -1 point. This leads to
the following scores:

Trial One:
	Our Agent: -89
	Base Agent: -2
Trial Two:
	Our Agent: -100
	Base Agent: -22
Trial Three:
	Our Agent: -123
	Base Agent: -68

This may not be the best scoring metric, but for the sake of a quick evaluation, it will do. The 
important thing to note to me here, is that out of all three trials, neither agent was able to 
capture more bugs than they let get to the exit (although the Base Agent got very close on the first
trial, only to do progressively worse in further trials). Just looking at the scores, it would seem 
that our agent is developing in the wrong direction, with substantially worse scores, but, this is 
because the agent has the ability to conserve its power, by using lower power settings and shutting 
the power down completely when unneccessary. This allows the agent to run longer, which in turn allowed
for more bugs to spawn and potentially escape. This ability for our agent to "survive" longer, I believe 
would benefit our agent in the extreme long-term, as it would have more time to learn compared to the
Base Agent, but I do not believe that in its current state the difference in power conservation would 
lead to anything beyond marginal improvements unless the game was run with an incredible amount of 
starting power. If you ignore the bugs that did escape, then our agent already performed better than the
Base Agent in two out of three trials, although this could also be chalked up to luck until the reward
function is altered in the next step of the assignment.

By the end of the assignment, our agent began to score much greater every trial, and after implementing
Q-learning, it scored the following:
Trial 1: 217 captures, 86 misses
Trial 2: 134 captures, 133 misses
Trial 3: 169 captures, 79 misses

The scores from these final trials completely blow the Base Agent out of the water, and with higher
starting powers, it would become increasingly different as our agent would continue to learn at a 
drastically faster pace than the Base Agent.


d) My first try for altering the reward function was to simply remove the step where the utility value
was divided by the number of attempts that action was made. My idea behind this was to prevent actions
that have already proven themselves as effective to be devalued. This change caused my agent to capture
roughly 7 to 9 more bugs than the default reward function, never performing worse than the default function.

Mateo's idea was to add a base value to the utility value of an action each time that it succeeded, and 
additionally, multiply the stored utility value by 1.5x. The idea behind this, was to compound the agent's
interest in actions that have been rewarded multiple times. If it works multiple times, then it must be doing
something right, right? This reward function caused our agent to capture an average of 15 more bugs than the 
default function when compared head to head. Additionally, the agent never performed worse than the base agent,
or the previous reward function I tested, out of the several different trials. This seemed acceptable for now,
and will be improved upon as the reward function is made more robust in later in the assignment.

Ultimately, this reward function was replaced with the reward algorithm mentioned in Part 2b.

_______________________________________________________________________________________________________________________________

Part 2
a) When the agent encounters multiple actions with the same utility value, it now has a 50/50 chance to continue
with the action that is currently in progress. This way there is a better chance that the agent will continue 
targeting a bug successfully before changing actions.


b) We implemented Q-learning into our agent, using thisState and lastState to calculate the reward function.
Whenever the reward method is called, the Qmap for thisState is checked, and the highest utility value found
in the Qmap is stored as the Qmax (thisState is the successor state, since the reward function is always called 
immediately after a state-change, and the reward is given to the lastState). This Qmax is used in the formula
utility = (1 - .7)*utility + (.7(value + (.9*qmax)))
Where .7 is the arbitrary Learning Rate, reward is the arbitrary reward given (10 for success, -1 for failure), 
and .9 is the Discount Factor. I genuinely could not believe the incredible boost in results that implementing 
this algorithm caused. The first time I ran it, I thought there must've been some kind of mistake, because it 
caught roughly 240 bugs and only 50 or so escaped (to my chagrin, I did not record these numbers because I was
so sure that it was some insane amount of luck that brought these numbers about). I ran the trial three more 
times on the four tower map, and the results were as follows:

Trial 1:
	217 captures, 86 escapes
Trial 2:
	134 captures, 133 escapes
Trial 3:
	169 captures, 79 escapes

These numbers are better than every other trial I had run by a massive margin. I believed that the agent would 
still learn too slowly to reach a positive score until step 3, where similar states are compared. However, seeing
these results made me very excited to explore further upgrades.

_______________________________________________________________________________________________________________________________


Part 3
a) When the agent is deciding what action to perform, it first iterates through a list of all known states, and
checks those states for the repeated cells that contains bugs in the same places as the current state. If the compared
state contains at least two bugs in identical locations, it then checks the QMap for that state for any actions with 
a higher utility than the current QMap. If one of those actions is higher, then it chooses that action from the previously 
explored state. Regrettably, this change, alongside of a change to the reward function to now slightly punish actions 
that fail to capture a bug, caused the agent to become less consistently stellar than before, although it still achieved
positive scores.

