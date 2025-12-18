import jwt
import datetime

# Secret key from application.properties
SECRET_KEY = "9a02115a835ee03d5fb83cd8a468ea33e4090a6a27fc1c6f208176163f256035"

def generate_token(username):
    payload = {
        "sub": username,
        "iat": datetime.datetime.utcnow(),
        "exp": datetime.datetime.utcnow() + datetime.timedelta(days=1)
    }
    token = jwt.encode(payload, SECRET_KEY, algorithm="HS256")
    return token

if __name__ == "__main__":
    print(generate_token("banbann"))
