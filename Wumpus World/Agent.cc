//Nathan Sexton 2021

#include <iostream>
#include "Agent.h"
#include <time.h>
#include <stdio.h>
#include <stdlib.h>

using namespace std;



Agent::Agent ()
{
	Location location;
	srand(time(NULL)); //seed random selection
}

Agent::~Agent ()
{

}

void Agent::Initialize () //Initializes all relevant gamestate variables
{
	hasGold = false;
	atStart = true;
	hasArrow = true;
	location = Location(1,1);
	orientation = 0;
}

Action Agent::Process (Percept& percept)
{
	/* Location and Orientation printing
	cout << "\nX: " << location.X;
	cout << "\nY: " << location.Y;
	cout << "\nOrientation: " << orientation;
	cout << "\nHAS GOLD?: " << hasGold;*/

	Action action;

	if((location.X == 1) && (location.Y == 1))
	{
		atStart = true;
	}
	else
	{
		atStart = false;
	}
	int random = rand() % 3;


	//grabs gold if glitter is true
	if(percept.Glitter == true)
	{
		action = GRAB;
		this->hasGold = true;
	}
	//if agent is on the starting square with the gold, exits
	else if((atStart) && (hasGold))
	{
		action = CLIMB;
	}
	//If the agent is facing the Wumpus and in it's row, shoots
	else if((location.Y == 4) && (hasArrow == true) && (orientation == 0))
	{
		action = SHOOT;
		this->hasArrow = false;
	}
	//If the agent is facing the Wumpus and in it's column, shoots
	else if((this->location.X == 4) && (this->hasArrow == true) && (this->orientation == 1))
	{
		action = SHOOT;
		this->hasArrow = false;
	}
	//If none of the above are triggered, random is used to select a random action
	else
	{
		if(random == 0)
		{
			action = GOFORWARD;
				if (orientation == 0)
				{
				if (location.X < 4)
					{
						location.X++;
					}
				} 
				else if (orientation == 1)
				{
					if (location.Y < 4)
					{
						location.Y++;
					} 
				}
				else if (orientation == 2)
				{
					if (location.X > 1)
					{
						location.X--;
					}
				} 
				else if (orientation == 3)
				{
					if (location.Y > 1)
					{
						location.Y--;
					}
				}
				else{
					cout<<"orientation error\n\n";
				}
		}

		else if(random == 1)
		{
			action = TURNLEFT;
				if (orientation == 0)
				{
					orientation = 1;
				} else if (orientation == 1)
				{
					orientation = 2;
				} else if (orientation == 2)
				{
					orientation = 3;
				} else if (orientation == 3)
				{
					orientation = 0;
				}
		}

		else if(random == 2)
		{
			action = TURNRIGHT;
				if (this->orientation == 0)
				{
					this->orientation = 3;
				} else if (this->orientation == 1)
				{
					this->orientation = 0;
				} else if (this->orientation == 2)
				{
					this->orientation = 1;
				} else if (this->orientation == 3)
				{
					this->orientation = 2;
				}
		}

		else
		{
			cout << "Something went wrong in random selection\n";
		}
	}

	return action;
}

void Agent::GameOver (int score)
{

}

