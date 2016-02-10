create table users (
    strava_id bigint primary key, 
    name text, 
    token text
);

create table activities (
    strava_id bigint primary key,
    athlete_id bigint not null references users (strava_id),
    name text,
    type text,
    start_date timestamp with time zone,
    distance numeric,
    total_elevation_gain numeric,
    moving_time integer,
    elapsed_time integer,
    average_speed numeric,
    max_speed numeric
);

create table challenges (
    id serial primary key,
    name text,
    description text,
    start_date date,
    end_date date,
    activity_type text
);

insert into challenges (name, start_date, end_date, activity_type) values ('Iterate sykkel challenge 2015', '2015-01-01', '2016-01-01', 'Ride');
insert into challenges (name, description, start_date, end_date, activity_type) values ('Iterate vintersykkel challenge 2015/16', 'Desember - Februar', '2015-12-01', '2016-03-01', 'Ride');
insert into challenges (name, start_date, end_date, activity_type) values ('Iterate sykkel challenge 2016', '2016-01-01', '2017-01-01', 'Ride');
insert into challenges (name, start_date, end_date, activity_type) values ('Iterate løping challenge 2015', '2015-01-01', '2016-01-01', 'Run');
insert into challenges (name, start_date, end_date, activity_type) values ('Iterate løping challenge 2016', '2016-01-01', '2017-01-01', 'Run');
insert into challenges (name, start_date, end_date, activity_type) values ('Iterate ski challenge 2015/16', '2015-07-01', '2016-07-01', 'NordicSki');
