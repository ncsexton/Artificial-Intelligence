;;==========================================================================
;;
;; STARTER FILE FOR CSC 4240/5240 PROGRAM #1: Eliza
;;==========================================================================

;;----------------------------------------------------------------------------
;; eliza: top-level function which, when given a sentence (no
;; punctuation, please!), comes back with a response like you would.

( defun eliza ( sentence )
  ( respond ( change-pros sentence ) database ) )

;;----------------------------------------------------------------------------
;; change-pros: changes the pronouns of the sentence so that Eliza can
;; come back with the appropriately switched first and second person
;; references.

;;----------------------------------------------------------------------------
;;seeding the random state
( setf *randnum* (make-random-state t))

( defun change-pros ( sentence )
  ( cond 
    ( ( null sentence ) nil )
    ( ( equal ( car sentence ) 'you )
      ( cons 'I ( change-pros ( cdr sentence ) ) ) )
    ( ( equal ( car sentence ) 'I )
      ( cons 'you ( change-pros ( cdr sentence ) ) ) )

    ;; CHANGE THIS: add more cases here of pronouns or other words
    ;; that should flip in order for this to work well
	( ( equal ( car sentence ) 'your )
      ( cons 'my ( change-pros ( cdr sentence ) ) ) )
    ( ( equal ( car sentence ) 'my )
      ( cons 'your ( change-pros ( cdr sentence ) ) ) )
	  


    ( t ( cons ( car sentence ) ( change-pros ( cdr sentence ) ) ) ) ) )

;;----------------------------------------------------------------------------
;; respond: given a sentence, looks through the database in search of
;; a matching pattern and the response; given the database response,
;; uses 'instantiate' to fill in the blanks, and returns the completed
;; response

( defun respond ( sentence db )
  ( cond
    ;; end of DB, return nil - should never really happen
    ( ( null db ) nil )

    ;; if the result of matching the sentence against the current
    ;; pattern is a success, produce this response
    ( ( success ( setq result ( match sentence ( first ( car db ) ) ) ) )
      ( instantiate result ( nth ( + 1 (random 3) ) ( car db ) ) ) )

    ;; otherwise, keep looking through the DB
    ( t ( respond sentence ( cdr db ) ) ) ) )

;;----------------------------------------------------------------------------
;; match: if there is not a match between this pattern and this data,
;; returns 'fail;' otherwise, returns the sentence in partitioned
;; format

( defun match ( data pattern )
  ( cond
    ;; end of both data and pattern; a match
    ( ( and ( null data ) ( null pattern ) ) nil )

    ;; end of pattern, but not end of data; no match
    ( ( null pattern ) fail )

    ;; end of data, but not end of pattern; if the pattern starts with
    ;; a variable, eat it and try and match the rest of the pattern to
    ;; the null sentence (will only work if all variables); otherwise,
    ;; fail
    ( ( null data ) 
      ( cond
	( ( variablep ( car pattern ) )
	  ( if ( success ( setq result ( match data ( cdr pattern ) ) ) )
	      result
	    fail ) )
	( t fail ) ) )


    ;; first item of data and pattern are identical; if the rest of it
    ;; matched, return the first item cons'ed with the rest of the
    ;; partitioned sentence; otherwise, fail
    ( ( equal ( car data ) ( car pattern ) )
      ( if ( success ( setq result ( match ( cdr data ) ( cdr pattern ) ) ) )
	  ( cons ( list ( car data ) ) result )
	fail ) )

    ;; first item of pattern is a variable; if the rest of the data
    ;; (minus the first word, matched to the variable) is a match with
    ;; all of the pattern, return the appropriate stuff; if all of the
    ;; data (variable eats nothing) matches the rest of the pattern,
    ;; return appropriate stuff; else, fail.
    ( ( variablep ( car pattern ) ) 
      ( cond
	;; variable eats nothing;  () is put in partitioned sentence
	( ( success ( setq result ( match data ( cdr pattern ) ) ) )
	  ( cons () result ) )
	;; variable eats one word; word is cons'ed into the first
	;; element of the partitioned sentence, assuming that the step
	;; before an actual match word would be a ()
	( ( success ( setq result ( match ( cdr data ) pattern ) ) )
	  ( cons ( cons ( car data ) ( car result ) ) ( cdr result ) ) )
	;; otherwise, fail
	( t fail ) ) )

    ( t fail ) ) )

;;----------------------------------------------------------------------------
;; instantiate: takes a partitioned sentence and the response it has
;; been matched to and generates the appropriated completed response

( defun instantiate ( partitioned response )
  ( cond
    ( ( null response ) nil )
    ;; numbers indicate what part of the partitioned sentence to
    ;; insert into the response
    ( ( numberp ( car response ) )
      ( setq index ( - ( car response ) 1 ) )
      ( append ( nth index partitioned )
	     ( instantiate partitioned ( cdr response ) ) ) )
    ( t ( cons ( car response )
	     ( instantiate partitioned ( cdr response ) ) ) ) ) )

;;---------------------------------------------------------------------------
;;
;;  			     helping functions
;;
;;---------------------------------------------------------------------------

( setq fail '-1 )

( defun success ( result )
  ( not ( equal result fail ) ) )

( defun variablep ( word )
  ( equal word '0 ) )


;;---------------------------------------------------------------------------
;;
;;  			         database
;;
;;---------------------------------------------------------------------------

;; CHANGE THIS: add more to this database so that the interaction is
;; more interesting and communicative and so that Eliza sounds like you 
;; would sound in the same conversation!
;;---------------------------------------------------------------------------

( setq database
       '(
	 ;; example greetings/farewells -- change them to sound like you
	 ( (Hello 0)
	   (Good to see you - how are you feeling today?) 
	   (Glad to see you - how have you been feeling?)
	   (Happy to see you - are you feeling well today?))
	 ( (0 you came here because 0)
	   (A lot of people come here for that reason so you are not alone.) 
	   (Many people come here for the same reason.)
	   (You are not the only one.))
	 ( (0 doing well 0)
	   (Glad to hear you are doing well.) 
	   (That is good to hear - is there any particular reason?)
	   (Happy to hear it - care to tell me why?) )
	 ( (0 feeling well 0)
	   (Glad to hear you are feeling well - any reason why?) 
	   (That is good to hear - is there any particular reason?)
	   (Happy to hear it - care to tell me why?) )
	   
	 ( (0 Goodbye 0)
	   (See ya - lets talk again soon.) 
	   (Bye bye - lets talk again soon.)
	   (Toodles!) )
	 ( (0 bye 0)
	   (See ya - lets talk again soon.) 
	   (Bye bye - lets talk again soon.)
	   (Toodles!) )


	 ;; misc convo catches
	 ( (0 game 0)
	   (I love games! What is your favorite?) 
	   (I enjoy playing games - do you have a favorite?)
	   (Do you have a favorite game?) )
	 ( (0 games 0)
	   (I love games! What is your favorite?) 
	   (I enjoy playing games - do you have a favorite?)
	   (Do you have a favorite game?) )
	 ( (0 your favorite 0)
	   (What makes it your favorite?) 
	   (Is there anything specific that makes it your favorite?) 
	   (Why is that your favorite?) )
	   
	 ( (0 the weather 0)
		(I hope it will snow soon - winter is my favorite season.) 
		(The weather can be so unpredictable at times.)
		(Do you have a favorite season?) )

	 ;; feelings
	 ( (0 you think 0)
	   (Why is it that you think 4 ?) 
	   (Is there a reason you think 4 ?)
	   (Why do you think 4 ?) )
	 ( (0 you feel like 0)
	   (Why is it that you feel like 4 ?) 
	   (Is there a reason you feel like 4 ?)
	   (Why do you feel like 4 ?) )
	 ( (0 complicated 0)
	   (What makes it so complicated?) 
	   (Is there a reason it is so complicated?) 
	   (Why is it complicated?) )
	 ( (0 interesting 0)
	   (What makes it so interesting?) 
	   (Is there a reason it is so interesting?) 
	   (Why is it interesting?) )
	 ( (0 you remember 0)
	   (How often do you think about 4 ?) 
	   (Do you often think about 4 ?)
	   (How often do you find yourself thinking about 4 ?) )
	 ( (0 you hate 0)
	   (Hate is a strong word.) 
	   (Why do you hate 4 ?)
	   (I have heard that hate can lead to the dark side.) )
	 ( (0 bored 0)
	   (Boredom is as old as people.) 
	   (Everyone gets bored sometimes.)
	   (I hope I do not bore you.))
	 ;; the catch-alls
	 ( (0) 
	   (Would you mind telling me more about that?) 
	   (Could you elaborate further?) 
	   (Could you expand on that?) ) ) )

