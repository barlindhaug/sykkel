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
