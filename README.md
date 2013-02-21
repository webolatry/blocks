A board game designed by a friend of mine. Ultimately there will be multiple levels, the current implementation has only one, for demonstration.

The gameplay is a bit tricky. The objective is to get the two gold pieces to connect. 

Pieces move when tapped. The trick is that a piece will move only when it is free to move in one direction, and one direction only. 

A piece is free to move in a particular direction if there isn't another piece in the way, or the edge of the board. If a piece looks like it is free to move up and to the right, it won't move. It must be free to move only in one direction.

After a piece moves it will merge with other pieces it touches that have the same color.
