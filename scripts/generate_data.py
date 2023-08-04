import random
import ijson
import pymysql
import json
import datetime
import requests
import chardet
import pandas as pd
from bs4 import BeautifulSoup
from pprint import pprint

con = pymysql.connect(
    host='204.216.223.231',
    port=3306,
    user='squinkis',
    db='musinco',
)


def random_date():
    start_date = datetime.date(2011, 1, 1)
    end_date = datetime.date(2021, 12, 31)
    return start_date + datetime.timedelta(
        # Get a random amount of seconds between `start` and `end`
        seconds=random.randint(
            0, int((end_date - start_date).total_seconds())),
    )


def random_phonenumber():
    return f"{random.randint(3000000000, 3999999999)}"


def pick_one_randomly(arr):
    return arr[random.randint(0, len(arr)-1)]


def generate_random16string():
    return ''.join(random.choice('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789') for n in range(16))


name_api = {
    'url': 'https://parseapi.back4app.com/classes/Complete_List_Names?limit=1000',
    'headers': {
        'X-Parse-Application-Id': 'zsSkPsDYTc2hmphLjjs9hz2Q3EXmnSxUyXnouj1I',
        'X-Parse-Master-Key': '4LuCXgPPXXO2sU5cXm6WwpwzaKyZpo3Wpj4G4xXK'
    },
    'data': {}
}

city_api = {
    'url_country': "https://countriesnow.space/api/v0.1/countries/positions",
    'url_city': "https://countriesnow.space/api/v0.1/countries/cities",
    'headers': {
    },

    'data_country': {},
    'data_city': {},
}

genre_api = {
    'url': 'https://everynoise.com/everynoise1d.cgi?scope=all'
}

instrument_api = {
    'url': 'https://www.allthemusicalinstrumentsoftheworld.com/index.php?page=List',
}

group_name_api = {
    'url': 'http://bandname.filiplundby.dk/api/v1/sentence',
    'headers': {
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
        'Content-Type': 'application/json',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/115.0'
    }
}


def instrument(cur):
    instr_res = requests.get(f"{instrument_api['url']}")
    soup = BeautifulSoup(instr_res.content, 'html.parser')
    instrument = []
    content = soup.find('tbody')
    list = content.find_all('tr')
    for li in list:
        instrument.append(li.td.text.lower())

    for i, instr in enumerate(instrument):
        if i < 5000:
            cur.execute(
                f'INSERT INTO musinco.instruments (instrument_id, instrument_name) VALUES("{i}","{instr}");')


def cities(cur):
    # Cities
    city_api['data_country'] = json.loads(requests.get(
        city_api['url_country']).content.decode('utf-8'))

    countries = [(x['name'], x['iso2'], x['lat'], x['long'])
                 for x in city_api['data_country']['data']]

    random.shuffle(countries)

    cities = []
    for country in countries[:50]:
        nation = (country[1])
        payload = {'iso2': nation}

        req = requests.post(
            city_api['url_city'], data=payload, headers=city_api['headers'])

        data = {}
        if req.status_code == 200:
            pprint("Adding cities for country: " + country[0])
            data = json.loads(req.content.decode('utf-8'))
            city_in_countries = data['data']
            cities_in_country = [(country[0], x, country[2], country[3])
                                 for x in city_in_countries]
            for city in cities_in_country:
                cities.append(city)
        random.shuffle(cities)
        cities = list(set(cities[:5000]))
        for city in cities[:5000]:
            cur.execute(
                f'INSERT INTO musinco.`position` (country, city, latitude, longitude) VALUES("{city[0]}", "{city[1]}", "{city[2]}", "{city[3]}");')


def genre(cur):
    genres = []
    genre_res = requests.get(f"{genre_api['url']}")
    soup = BeautifulSoup(genre_res.content, 'html.parser')
    soup = soup.find_all('td', class_='note')
    for i in range(0, len(soup), 2):
        tag1 = soup[i]
        tag2 = soup[i+1]
        genres.append((tag1.text, tag2.text))
    for genre in genres[:5000]:
        cur.execute(
            f'INSERT INTO musinco.genre (genre_id,genre_name) VALUES("{genre[0]}", "{genre[1]}");')


def artist(cur):
    name_api['data'] = json.loads(requests.get(
        name_api['url'], headers=name_api['headers']).content.decode('utf-8'))

    names = [(x['objectId'], x['Name']) for x in name_api['data']['results']]
    for name in names:
        cur.execute(
            f'INSERT INTO musinco.artist (artist_name) VALUES("{name[1]}");')


def user(cur):
    cur.execute('SELECT * FROM musinco.artist;')
    artists = cur.fetchall()
    cur.execute('SELECT position_id FROM musinco.`position`;')
    positions = cur.fetchall()
    for art in artists:
        random_index = random.randint(0, len(positions)-1)
        cur.execute(
            f"INSERT INTO musinco.users (user_id, username, email, first_name, last_name, codice_fiscale,based_near, professional_level, expertise_level, `multi-instrumentalism_level`) VALUES('{art[0]}','{art[1]}_nick', '{art[1]}@mail.com', '{art[1]}', '', '{generate_random16string()}','{positions[random_index][0]}', 'begin', 'begin', '1');")


def user_has_instrument(cur):
    cur.execute('SELECT user_id FROM musinco.users;')
    users = cur.fetchall()
    cur.execute('SELECT instrument_id FROM musinco.instruments;')
    instruments = cur.fetchall()
    for user in users:
        random_index = random.randint(0, len(instruments)-1)
        cur.execute(
            f"INSERT INTO musinco.user_has_instrument (user_id, instrument_id) VALUES('{user[0]}','{instruments[random_index][0]}');")


def user_plays_genre(cur):
    cur.execute('SELECT user_id FROM musinco.users;')
    users = cur.fetchall()
    cur.execute('SELECT genre_id FROM musinco.genre;')
    genres = cur.fetchall()
    for user in users:
        random_index = random.randint(0, len(genres)-1)
        cur.execute(
            f"INSERT INTO musinco.user_plays_genre (user_id, genre_id) VALUES('{user[0]}','{genres[random_index][0]}');")


def subgenre_of(cur):
    # Get Macro Genres
    macro_genres = ["pop", "rock", "rap", "hip hop", "r&b", "soul",
                    "funk", "jazz", "blues", "country", "folk", "classical"]
    genres_id = []
    for genre in macro_genres:
        # get all ids of macro genres
        cur.execute(
            f"SELECT genre_id FROM musinco.genre WHERE genre_name = '{genre}';")
        genres_id = cur.fetchone()[0]

        # get all subgenres of macro genres
        # aka all genres that contain the macro genre name
        subgenre = []
        cur.execute(
            f"SELECT genre_id FROM musinco.genre WHERE genre_name LIKE '%{genre}%';")
        subgenre = cur.fetchall()

        # insert subgenres into subgenre_of table
        for sub in subgenre:
            cur.execute(
                f"INSERT INTO musinco.subgenre_of (genre_id, subgenre_id) VALUES('{genres_id}','{sub[0]}');")


def musician_group(cur):
    # Get last artist id
    cur.execute(
        'SELECT artist_id FROM musinco.artist ORDER BY artist_id DESC LIMIT 1;')
    last_artist_id = cur.fetchone()[0]

    # Get names of groups
    group_names = []
    while len(group_names) < 100:
        group_name_res = requests.get(f"{group_name_api['url']}",
                                      headers=group_name_api['headers'])
        group_name_res = json.loads(group_name_res.content.decode('utf-8'))
        group_names.append(group_name_res[0])

    # Insert groups into artist table from last artist id
    for group in group_names:

        start_date = datetime.date(2011, 1, 1)
        end_date = datetime.date(2021, 12, 31)

        time_between_dates = end_date - start_date
        days_between_dates = time_between_dates.days
        random_number_of_days = random.randrange(days_between_dates)
        random_date = start_date + \
            datetime.timedelta(days=random_number_of_days)

        formatted_date = random_date.strftime('%Y-%m-%d')

        last_artist_id += 1
        cur.execute(
            f"INSERT INTO musinco.artist (artist_id, artist_name) VALUES('{last_artist_id}','{group}');")
        # Insert groups into musician_group table
        cur.execute(
            f"INSERT INTO musinco.musician_group (group_id, group_name, created_in) VALUES('{last_artist_id}','{group}','{formatted_date}');")


def music_venue(cur):
    names = ['bar', 'pub', 'club', 'theatre', 'arena', 'stadium', 'hall', 'opera house', 'amphitheatre',
             'auditorium', 'music hall', 'concert hall', 'recital hall', 'opera hall', 'opera theatre', 'opera']

    # query cities
    cur.execute('SELECT position_id , city FROM musinco.`position` LIMIT 5000;')
    cities = cur.fetchall()
    final_venues = []
    # create venues
    for city in cities:
        final_venues.append((f"{pick_one_randomly(names)} {city[1]}", city[0]))

    for venue in final_venues:
        pprint(f"Adding venue: {venue[0]}")
        cur.execute(
            f'INSERT INTO musinco.music_venue (name, placed_in, phone_number, email) VALUES("{venue[0]}", "{venue[1]}","{random_phonenumber()}","{"".join(venue[0].split())}@mail.com");')


def self_learning_session(cur):
    # query users
    cur.execute('SELECT user_id, based_near FROM musinco.users;')
    artists = cur.fetchall()
    # query music_venue
    # cur.execute('SELECT venue_id FROM musinco.music_venue;')
    # venues = cur.fetchall()

    for artist in artists:
        # query music_venue near user
        cur.execute(
            f'SELECT music_venue_id FROM musinco.music_venue WHERE placed_in = "{artist[1]}";')
        venues = cur.fetchall()
        if len(venues) > 0:
            cur.execute(
                f"INSERT INTO musinco.self_learning_session (artist_id, `date`, music_venue_id) VALUES('{artist[0]}', '{random_date()}', '{pick_one_randomly(venues)[0]}');")


def used_instrument(cur):
    # query learning sessions
    cur.execute(
        'SELECT session_id FROM musinco.self_learning_session;')
    sessions = cur.fetchall()
    # query instruments
    cur.execute('SELECT instrument_id FROM musinco.instruments;')
    instruments = cur.fetchall()

    for session in sessions:
        cur.execute(
            f"INSERT INTO musinco.used_instrument (session_id, instrument_id) VALUES('{session[0]}', '{pick_one_randomly(instruments)[0]}');")


def load_json(file):
    data = {}
    # open file
    with open(file, 'rb') as f:
        # read one line
        line = f.readline()
        # while line is not empty
        while line:
            buffer = line.decode('utf-8')
            json_data = json.loads(buffer)
            yield json_data
            line = f.readline()


def get_ISWC(row):
    if 'iswc' in row:
        return row['iswcs'][0]
    else:
        return ''


def get_genre(row, cur):
    ret_genre = ''
    if 'genres' in row and len(row['genres']) > 0:
        name_to_search = row['genres'][0]['name']
        cur.execute(
            f'SELECT genre_id FROM musinco.genre WHERE genre_name LIKE "%{name_to_search}%";')
        genre_id = cur.fetchone()
        if genre_id is None:
            ret_genre = ''
        else:
            ret_genre = genre_id[0]
    else:
        ret_genre = ''
    return ret_genre


def get_artist(row):
    ret_artist = ''
    for relation in row['relations']:
        if relation['type'] == 'composer':
            ret_artist = relation['artist']['name']
            break
    # if ret_artist == '':
    #     pprint(row)
    return ret_artist


def musical_work(file, cur):
    # build json data
    counter = 0
    failed = 0
    first_row = load_json(file)
    # get first row
    while counter < 2000:
        row = next(first_row)
        values = {
            'title': row['title'],
            'iswc': '',
            'genres': '',
            'artist': '',
        }
        values['iswc'] = get_ISWC(row)
        values['genres'] = get_genre(row, cur)
        values['artist'] = get_artist(row)

        # insert into db
        try:
            if values['genres'] == '':
                cur.execute(
                    f'INSERT INTO musinco.musical_work (title, iswc, artist) VALUES("{values["title"]}", "{values["iswc"]}", "{values["artist"]}");')
            else:
                cur.execute(
                    f'INSERT INTO musinco.musical_work (title, iswc, genre_id, artist) VALUES("{values["title"]}", "{values["iswc"]}", "{values["genres"]}", "{values["artist"]}");')
        except Exception as e:
            failed += 1
            pprint(e)

        counter += 1

    pprint(f"Failed: {failed}")

    # build query


def musical_work_played(cur):
    # session query
    cur.execute('SELECT session_id FROM musinco.self_learning_session;')
    sessions = cur.fetchall()
    # musical_work query
    cur.execute('SELECT musical_work_id FROM musinco.musical_work;')
    works = cur.fetchall()

    for session in sessions:
        cur.execute(
            f"INSERT INTO musinco.musical_work_played (session_id, musical_work_id) VALUES('{session[0]}', '{pick_one_randomly(works)[0]}');")


def group_user(cur):
    # query users
    cur.execute('SELECT user_id FROM musinco.users;')
    artists = cur.fetchall()
    # query musician_group
    cur.execute('SELECT group_id FROM musinco.musician_group;')
    groups = cur.fetchall()
    try:
        for group in groups:
            group_len = random.randint(2, 5)
            group_members = list(
                set([pick_one_randomly(artists)[0] for i in range(group_len)]))
            for member in group_members:
                cur.execute(
                    f"INSERT INTO musinco.group_user (group_id, user_id) VALUES('{group[0]}', '{member}');")
    except Exception as e:
        pprint(e)


with con.cursor() as cur:
    pprint("Starting to generate data")
    # instrument(cur)
    # cities(cur)
    # genre(cur)
    # artist(cur)
    # user(cur)
    # user_has_instrument(cur)
    # user_plays_genre(cur)
    # subgenre_of(cur)
    # music_venue(cur)
    # musician_group(cur)
    # self_learning_session(cur)
    # used_instrument(cur)
    # musical_work('D:\work.tar\work\mbdump\work', cur)
    # musical_work_played(cur)
    # group_user(cur)
    pprint("Finished generating data")

con.commit()
con.close()