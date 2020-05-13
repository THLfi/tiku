#!/bin/bash

# Asetetaan salasana
PW="salasana"

# Asetetaan domain
DOMAIN="https://sampo.thl.fi"

# Asetetaan URL:t
CSRF_URL="$DOMAIN/pivot/csrf"
URL="$DOMAIN/pivot/prod/api/epirapo/c19caseproto/fact_epirapo_c19caseproto"

# Asetetaan cookie-tiedosto
COOKIE=/tmp/cookie.txt

# Asetetaan parametrit cURL:lle
CURL_OPTS="-s4 --compressed --connect-timeout 3 -b $COOKIE -c $COOKIE"

# Haetaan istunnon ajan voimassaoleva CSRF-token cURL-ohjelmalla
CSRF=$(curl $CURL_OPTS $CSRF_URL)

# Kirjaudutaan cURL-ohjelmalla ja asetetaan oikea arvo CSRF-parametrille
curl $CURL_OPTS -d "password=$PW&csrf=$CSRF" $URL

# Luetaan data JSON-formaatissa cURL-ohjelmalla
curl $CURL_OPTS $URL.json

# Poistetaan cookie-tiedosto
rm -f $COOKIE
