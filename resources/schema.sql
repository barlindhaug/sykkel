create table users (
    strava_id bigint primary key,
    name text,
    picture text,
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
    activity_type text,
    type text,
    field text,
    results integer
);

insert into challenges (name, start_date, end_date, activity_type, type) values ('Iterate sykkel challenge 2015', '2015-01-01', '2016-01-01', 'Ride', 'totals');
insert into challenges (name, description, start_date, end_date, activity_type, type) values ('Iterate vintersykkel challenge 2015/16', 'Desember - Februar', '2015-12-01', '2016-03-01', 'Ride', 'totals');
insert into challenges (name, start_date, end_date, activity_type, type) values ('Iterate sykkel challenge 2016', '2016-01-01', '2017-01-01', 'Ride', 'totals');
insert into challenges (name, description, start_date, end_date, activity_type, type) values ('Iterate sykle til jobben 2016', '19. april - 17. juni', '2016-04-19', '2016-06-18', 'Ride', 'totals');
insert into challenges (name, start_date, end_date, activity_type, type) values ('Iterate løping challenge 2015', '2015-01-01', '2016-01-01', 'Run', 'totals');
insert into challenges (name, start_date, end_date, activity_type, type) values ('Iterate løping challenge 2016', '2016-01-01', '2017-01-01', 'Run', 'totals');
insert into challenges (name, start_date, end_date, activity_type, type) values ('Iterate ski challenge 2015/16', '2015-07-01', '2016-07-01', 'NordicSki', 'totals');
insert into challenges (name, start_date, end_date, activity_type, type, field, results) values ('Iterate topp 5 lengste sykkelturer', '2000-01-01', '2100-01-01', 'Ride', 'top', 'distance', 5);
insert into challenges (name, start_date, end_date, activity_type, type, field, results) values ('Iterate topp 5 klatringer sykkel', '2000-01-01', '2100-01-01', 'Ride', 'top', 'total_elevation_gain', 5);
insert into challenges (name, start_date, end_date, activity_type, type, field, results) values ('Iterate topp 5 lengste løpeturer', '2000-01-01', '2100-01-01', 'Run', 'top', 'distance', 5);
insert into challenges (name, start_date, end_date, activity_type, type, field, results) values ('Iterate topp 5 lengste skiturer', '2000-01-01', '2100-01-01', 'NordicSki', 'top', 'distance', 5);
