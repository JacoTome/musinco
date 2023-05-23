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
		const url = "http://localhost:3002/2";
		const response = await fetch(url);
		const data = await response.json();
		this.setState({ user: data });
	}

	
	createCard = (user) => {
		return <Card user={user.musician.value} key={user.name} />;
	};

	render() {
		return (
			<div style={{ display: "block", marginTop: "200px" }}>
				<h1>User Matching With You </h1>
				<div style={{ display: "flex" }}>
					{
						this.state.user.map(obj => {
							return <Card user={obj} key={obj.musician.value} />							
					}
					)}
				</div>
			</div>
		);
	}
}
