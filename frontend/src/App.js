import "./App.css";
import Genre from "./components/Genre";
import Header from "./components/Header";
import UserSuggestion from "./components/UserSuggestion";
function App() {
	return (
		<div className="App">
			<Header />
			<UserSuggestion />
			<Genre />
		</div>
	);
}

export default App;
