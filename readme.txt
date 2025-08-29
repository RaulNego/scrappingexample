Scrapping example of getting contacts from 1000 websites.

To run project:
run the elasticsearch docker container using the docker compose file, start the spring project, access the 2 API endpoints,
  one for scrapping, localhost:8080/api/pages/scrape,
  the other for querring, http://localhost:8080/api/pages/search-simple?name=___&phone=___&website=___&input_facebook=___
example: http://localhost:8080/api/pages/search-simple?name=Acorn%20Law%20P.C.&phone=&website=https://acornlawpc.com&input_facebook=https://www.facebook.com/acornfurnitureworkshops

Thought process:

Tools:
The language used is Java as, even though python is much faster to validate proof of concepts, we can use threads in java, wich saves us a lot of computing time.
Scrapping framework is Playwright, I have experience with Selenium and also the requests lib in python, out of all of those I find Playwright to be fastest. Selenium is more safe but overall slower.
In order to have APIs and also test them, I needed a web framework. I chose Spring because I know it is fast to set up even though I have no previous Spring experience.
As it is sugested to use searchengines like elasticsearch, i added elasticsearch.

Implementation:
After initial checks on the websites, I noticed that about 800 of them would not work, therefore I isolated the working ones while building the scrapper (final solution rescans everything).
Majority of websites fall in 3 categories
- contacts on front page
- contacts in about/aboutus page
- contacts in the contacts page
Therefore the first thing is to find an /about or /contact page, if found, search there, if not, search main page

Once we have the right URL, use regex to search for phone, email address and URLs to social websites
a lot of hits are false pozitive, but we know that all the contacts are clustered toghether, therefore I check the distance (of characters) between all the possible outcomes, resulting usualy in the real contacts.
If a website has all 3 of those fields, usually the scrap will be successful, if it misses 2 of them, the result will most likely be a false pozitive.

This is the alghoritm. To run it, I start ~ 10 threads or any number that the hardware might allow and let it run, each his slice of the websites. Usual times to run takes arond 240 secounds
The data extracted then is loaded to elasticsearch.
Once inside, it can be querried by the /seach-simple endpoint.

When it comes to what alghoritm I use to properly querry, I just match the phone/facebook link/ name/ website 1 to 1 with the fields in elastic search and pray the scoring system will do its job :).

Some refactoring to code might be in order, for example using the body to send all the data to the API instead of just using the queries, but in order to save time I chose to forgo such things as it is simple but takes time. I would call this solution almoost production ready for this reason.
