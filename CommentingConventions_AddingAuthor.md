# Introduction #

This post explains how to:
1) auto-generate a comment for any component in eclipse (java based)
2) auto-generate author field in all components

Such comments appear automatically on hovering on the component and can be auto-extracted as JavaDocs page

Comments are used for better documentation and integration between the team members.

Adding author tag is important because it facilitates contacting the writer(s) of the methods for inquiries or problem solving.

# Details #

<b>1) Auto generate comments for any component in eclipse:</b>

above any method : type / and press enter

<b>2) Adding author information for any component (especially class or method)</b>

 auto-generate the comments section
 add inside that: @author your\_name (your email)

<b>2) To auto-generate author tag in the comments section </b>

<i>For methods comments: </i>

a) In eclipse: Windows -> preferences -> java -> code style ->code templates -> comments -> methods -> edit

b) copy and paste the following:
> /
  * ${tags}
  * 
  * @author
  * Passant El.Agroudy (passant.elagroudy@gmail.com)
  * 

<i>For class comments: </i>

choose types instead of methods