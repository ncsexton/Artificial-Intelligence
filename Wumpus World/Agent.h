//Nathan Sexton

#ifndef AGENT_H
#define AGENT_H

#include "Action.h"
#include "Percept.h"
#include "Location.h"
#include "Orientation.h"

class Agent
{
public:
	Agent ();
	~Agent ();
	void Initialize ();
	Action Process (Percept& percept);
	void GameOver (int score);

	//Gamestate Booleans
	bool hasGold;
	bool atStart;
	bool hasArrow;
	int orientation;

	//Agent state object
	Location location;
};

#endif // AGENT_H
