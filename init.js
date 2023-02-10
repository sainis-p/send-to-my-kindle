print("Started Adding the Users.");
db = db.getSiblingDB("SendToKindle");
db.createUser({
  user: "appUser",
  pwd: "appPassword",
  roles: [
    {
      role: "readWrite",
      db: "SendToKindle"
    }
  ]
});
print("Finished Adding the Users.");