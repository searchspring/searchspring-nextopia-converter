cat tmp/*.log \
    | grep -v AutoComplete \
    | grep buildRequest \
    | sed -e 's~.*https://api.nextopiasoftware.com/return-results.php\?\(.*\)~\1~g' \
    | tr "&" "\n" \
    | sed -e 's~\(.*\)=.*~\1~g' \
    | sort \
    | uniq -c 