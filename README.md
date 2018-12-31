plant-analyzer
=========

Description
----------------------------------------------------
plant picture to plant name converter


Build the static frontend
-------------------------
```bash
	cd frontend
```

generate the html
```bash
	npm start
```

show the html
```bash
	http-server ./dist
```

optional: promote coming changes via grunt
```bash
	grunt watch
```


view http://localhost:8080


Build the dynamic backend
-------------------------

```bash
	cd backend
```
```bash
	mvn install; heroku local
```

view http://localhost:5000

Deploy the dynamic backend
-------------------------

create a github repository (without readme)
	https://github.com/new

push to the new repo
```bash
	git init
	git commit -am "initial commit"
	git remote add origin https://github.com/<your user name>/plant-analyzer.git
    git push -u origin master
```

create heroku app
```bash
	heroku create plant-analyzer
```

connect heroku app with github
	https://dashboard.heroku.com/apps/plant-analyzer/deploy/github

enable automatic deploys

test manual deploy
	click "Deploy Branch" and watch the logs
	wait for "Your app was successfully deployed"

Open your deployed app
	https://plant-analyzer.herokuapp.com/systeminfo



Deploy the static frontend
-------------------------

create a new netlify app
https://app.netlify.com/start

connect to your github repo

deploy your site

open "domain settings"-> "Custom Domains" -> "edit site name"
change the generated site name to "plant-analyzer"

open https://plant-analyzer.netlify.com/

WIP
-------------------------

github account
git add --all; git commit -m"."; git push -u origin master
https://github.com/rherrajan/twoTier

netlify account
https://two-tier.netlify.com/
npm start

heroku account
https://two-tier.herokuapp.com/
mvn install; heroku local
heroku logs --tail --app two-tier

grunt
nvm install 5.0
nvm use 5.0
npm install -g grunt-cli
npm install grunt-metalsmith --save-dev
npm install grunt-contrib-watch --save-dev

grunt watch

http-server dist



build with [Clanneyder](https://github.com/rherrajan/clanneyder)

