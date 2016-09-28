Custom Set Utility for DeckedBuilder's Card Cam
GUI to extract and recombine card data into new sets.

Requires Java 8. Probably only supports windows versions of Deckedbuilder

-

USAGE:

-

Step 1: Generate Card Data Files

First, Launch Decked Builder, go to the card cam page, and, one by one, check each set checkbox, let the set download, and uncheck that set's checkbox. Close Decked Builder.

Launch the included jar file to bring up the program GUI. Click the "Extract Card Files" button. This will extract the card data from deckedbuilder's files to make building custom sets possible.

Extracting the cards may take some time. The progress bar at the bottom of the window will show the progress of the extraction process. A dialog will open after the process is complete;
press "OK" in this dialog to return to the main program.

This step only needs to be performed once.

-

Step 2: Generate the custom set

To specify the cards to be put in the set, enter them, one per line, in the text area in the program window. 
Special characters such as the "Æ" in Æther Adept should be entered as they are.
The cards "Plains", "Island", "Swamp", "Mountain", and "Forest" are excluded from the custom set.

Press the "Save Set" button to save the set. Enter a set name (unique from any other set), and a category to display the set under in the cardcam menu.

An internet connection is required to export a set, in order to connect to http://api.deckbrew.com/ and find the multiverse ids of each card.

Exporting a set may take a bit of time (though it should be considerably less than the card extraction process).

When Decked Builder is launched, the custom set should be displayed on the cardcam's set selection list.

-

Card Cam Display:

To hook the card cam display into cardcam, you first need to press the "Setup card display hook" to setup deckedbuilder to interface with the card display. To open the card display, press the "open card display" button to bring up the card display window.

-

Updating: 

Whenever decked builder is updated, perform step 1 again to be able to export sets with cards added in those updates. 

If a database update removes custom sets from the cardcam menu, press the "Reinject Custom Sets" button to restore any previously generated custom sets.
