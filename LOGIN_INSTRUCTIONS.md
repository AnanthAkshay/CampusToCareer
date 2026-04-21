# Login Instructions (Dev Mode)

Since SMTP email is not configured, the system is running in **DEV MODE** where OTPs are printed to the console instead of being emailed.

## How to Login:

1. **Go to the login page**: http://localhost:9090/pages/login-otp.jsp

2. **Enter USN**: `1MS24IS013` (or any other student USN from the CSV)

3. **Click "Send OTP"**

4. **Check Docker logs** to get the OTP:
   ```bash
   docker logs placement-app --tail 20
   ```
   
   Look for output like:
   ```
   ========================================
   📧 DEV MODE: Email sending disabled
   ========================================
   To: 1ms24is013@rit.edu
   User: AKSHAY A
   OTP: 123456
   ========================================
   ```

5. **Enter the OTP** from the logs into the verification page

6. **Login successful!**

## Available Test Users:

All students from the CSV file have been imported. Some examples:
- `1MS24IS001` - AADITYA V
- `1MS24IS013` - AKSHAY A
- `1MS24IS054` - HRISHIKESH S SHETTY
- `1MS24IS094` - RAVNISH SHEKHAR

Default password (if using password login): `student123`

## To Enable Real Email:

Edit `docker-compose.yml` and set these environment variables:
```yaml
SMTP_USERNAME: "your-gmail@gmail.com"
SMTP_PASSWORD: "your-app-password"
SMTP_FROM_EMAIL: "noreply@ritplacement.edu"
```

Then restart: `docker-compose down && docker-compose up -d --build`
