# Iterate Sykkel Challenge

## Setup
### Database
    psql
    create user "app-sykkel" with password 'sykkel';
    create database "app-sykkel";

exit

    psql -U "app-sykkel"
    create table users (strava_id bigint, name text, token text);

### Environment variables
    export STRAVA_API_KEY=""
    export STRAVA_CLIENT_SECRET=""
    export DB_PASS="sykkel"
    export DB_USER="app-sykkel"
    export DB_PORT="5432"
    export DB_HOST="localhost"

## Development
    lein ring server

## Deployment
 - Add app.iterate.no as a remote:
    git remote add iterate dokku@app.iterate.no:sykkel


 - Push to app.iterate.no:
    git push iterate master
