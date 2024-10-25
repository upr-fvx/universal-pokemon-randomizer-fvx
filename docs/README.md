# Developer info

This folder contains the source for the Randomizer's website.
(currently hosted at https://upr-fvx.github.io/universal-pokemon-randomizer-fvx)

This website runs off Jekyll, and is deployed using GitHub actions using 
GitHub Pages. The site is updated whenever the master branch is pushed to, 
so it is recommended you deploy the site locally first to test it, especially
if you are new to Jekyll. To do that, you must install Jekyll locally:
https://jekyllrb.com/docs/installation/

In short, Jekyll allows articles to be written using Markdown instead of HTML.

Other than that, development should be quite straight forward. Developers 
are encouraged to add/edit articles to the "wiki" (name pending) to match
code additions/changes.

Just remember to use `{{ site.baseurl }}` in front of any internal hyperlink. 
E.g. `[check out the about page]({{ site.baseurl }}/about.html)` is correct,
`[check out the about page](/about.html)` is not. Also, due to technical reasons
_this_ page is deployed to the website, but please do not link to it.