# LAB 5 - Employee Management App: written answers

Last name / First name: ........................  Group: 3 LBC-BIS  Date: ............

## Comprehension questions

**1. What is the difference between `onCreate` and `onDestroy`? Why open the database in one and close it in the other?**

`onCreate` is the first lifecycle callback: Android calls it once when the Activity is created, so it is where the layout is loaded and resources are prepared. `onDestroy` is the last one, called just before the Activity is removed from memory. Opening the database in `onCreate` makes it available for the whole life of the screen, and closing it in `onDestroy` releases the connection so nothing leaks.

**2. What does `finish()` do after `startActivity(...)`? What would happen if you removed it from `HomeActivity.gotosignin`?**

`finish()` closes the current Activity and removes it from the back stack. Without it, Home would stay under the sign-in screen: after "Sign out", pressing Back would reopen the admin home without signing in again.

**3. How does the "Sign in" button know to call `login`? What signature must this method have?**

The layout declares `android:onClick="login"` on the button. On a click, Android looks in the Activity that displays the layout for a method with that exact name. It must be `public void login(View view)`: public, returning `void`, with a single `View` parameter. Otherwise the app crashes on the click.

**4. Why use `?` placeholders in `rawQuery` instead of concatenating the username into the query?**

With a placeholder, SQLite receives the value as data, separately from the SQL text, so it can never be interpreted as SQL. With concatenation, a user could type something like `' OR '1'='1` and change the meaning of the query (SQL injection), for example to sign in without a password. Placeholders also handle quotes in names correctly.

**5. What do the numbers returned by `insert`, `update`, and `delete` mean?**

`insert` returns the row id of the new row, or `-1` if the insertion failed (for example the CIN primary key already exists). `update` and `delete` return the number of rows affected: `0` means no row matched (employee not found), a value greater than 0 means the operation succeeded.

**6. In `afficherEmployees`, why call `removeAllViews()` before the loop? What would you see otherwise?**

The container keeps the cards added during the previous display. `removeAllViews()` empties it before rebuilding. Without it, each refresh (after an edit, a delete or a search) would add the whole list again under the old one: duplicated cards, and deleted employees still visible.

**7. Why copy Cursor data into an `ArrayList` and then close the Cursor instead of keeping it open?**

A Cursor holds a native database resource; left open, it leaks memory and can block the database from closing cleanly. Copying the rows into `User` objects lets us close it immediately, and the list stays usable afterwards (for the click listeners of the cards) without depending on the database connection.

**8. What does the Spinner do during registration, and where do its values come from?**

The Spinner is a drop-down list used to choose the role of the new account, read with `getSelectedItem().toString()`. Its values come from a string array in `res/values/arrays.xml` (`@array/roles`: Admin, Employee). The role is not typed by hand, so it is always a valid value.

## Bonus exercises

| # | Exercise | Where it is implemented |
| --- | --- | --- |
| 1 | Registration button label | String `s_inscrire` ("Sign up"), used by the button in `activity_signup.xml` |
| 2 | Welcome message | `HomeActivity.onCreate` reads the `user` extra and fills `txtWelcome` with "Welcome, admin" |
| 3 | Back button | "Back to Home" button in `activity_users_list.xml`, handled by `UsersListActivity.retourAccueil` |
| 4 | Delete confirmation | `UsersListActivity.confirmerSuppression` shows an `AlertDialog` before deleting |
| 5 | Security | See below |
| 6 | Search | `txtSearch` field + `TextWatcher`; `UserDB.getEmployees(filter)` filters with `Firstname LIKE ?` |

**Bonus 5 - proposed solution.** The problem: the public registration screen offered the Admin role, so anyone could create an administrator account. The solution: the public screen now only offers the Employee role (`@array/roles_public`), and `SignupActivity.signup` also refuses the Admin role in code when the screen was not opened by an administrator. Admin accounts can only be created from the admin home ("Create a user"), which opens registration with the `EXTRA_FROM_ADMIN` extra after a successful admin sign-in.

## Test checklist (run on an API 36 emulator)

| No. | Action | Result |
| --- | --- | --- |
| 1 | Launch the app | Sign-in screen appears |
| 2 | "Sign in" with no data | "Please fill in all fields" |
| 3 | admin + wrong password | "Incorrect username or password!" |
| 4 | admin / 1234 | Home screen opens |
| 5 | "Sign out" | Back to sign-in; Back does not reopen Home |
| 6 | Register with an empty field | "Please fill in all fields" |
| 7 | Register an employee | Back to sign-in |
| 8 | Register with an existing CIN | "Registration failed" |
| 9 | Sign in as the new employee | "Access is restricted to administrators!" |
| 10 | Admin: list, edit, delete | List and count update, Toast messages shown |
| 11 | Close and relaunch | Employee records still present |
