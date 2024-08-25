# Now Playing

Kotlin library to connect to a BluOS player. Based on [v1.5 of the BluOS API][bluos-api-v1.5].

## Running it

```shell
PLAYER_HOST='<IP or host of player>' ./gradlew run
```

### Settings

| Variable      | Description                                                    |
| ------------- | -------------------------------------------------------------- |
| PLAYER_SCHEME | The scheme over which to reach the player. Defaults to `http`. |
| PLAYER_HOST   | Where to reach the player. Required.                           |
| PLAYER_PORT   | The port on which the player is listening. Defaults to 11000.  |

[bluos-api-v1.5]: https://content-bluesound-com.s3.amazonaws.com/uploads/2022/07/BluOS-Custom-Integration-API-v1.5.pdf
