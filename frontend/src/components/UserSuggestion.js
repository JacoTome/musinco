import React, { Component } from "react";
import Card from "./Card";

export default class UserSuggestion extends Component {
	constructor(props) {
		super(props);
		this.state = {
			user: [],
			loading: true,
		};
	}

	async componentDidMount() {
		const url = "https://jsonplaceholder.typicode.com/users";
		const response = await fetch(url);
		const data = await response.json();
		this.setState({ user: data, loading: false });
	}

	createCard = (user) => {
		return <Card user={user} key={user.name} />;
	};

	render() {
		return (
			<div style={{ display: "block", marginTop: "200px" }}>
				<h1>User Matching With You </h1>
				<div style={{ display: "flex" }}>
					{this.state.loading || !this.state.user ? (
						<div>Loading...</div>
					) : (
						this.state.user.map(this.createCard)
					)}
				</div>
			</div>
		);
	}
}
